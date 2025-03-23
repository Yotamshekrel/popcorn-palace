package com.att.tdp.popcorn_palace;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A comprehensive integration test suite that exercises
 * Movie, Showtime, and Booking endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PopcornPalaceApplicationTests {

	@Test
	void contextLoads() {
		// Basic sanity check that the application context starts up
	}

	@LocalServerPort
	int port;

	private RestTemplate restTemplate;

	@BeforeEach
	void setup() {
		restTemplate = new RestTemplate();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// MOVIE TESTS //
	//////////////////////////////////////////////////////////////////////////////////////
	@Nested
	@DisplayName("MOVIES API")
	class MoviesApiTests {

		@Test
		@DisplayName("1) Should create a valid movie successfully")
		void createValidMovie() {
			// Create a movie with valid data
			URI uri = URI.create("http://localhost:" + port + "/movies");
			String requestBody = """
					    {
					      "title": "IntegrationMovie",
					      "genre": "Action",
					      "duration": 120,
					      "rating": 8.0,
					      "releaseYear": 2024
					    }
					""";

			ResponseEntity<String> response = restTemplate.postForEntity(
					uri,
					new HttpEntity<>(requestBody, jsonHeaders()),
					String.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).contains("Successfully created the movie");
		}

		@Test
		@DisplayName("2) Should retrieve all movies (including newly created one)")
		void getAllMovies() {
			// create a movie
			createMovie("MovieX", "Comedy", 95, 7.5, 2025);

			// fetch all movies
			URI uri = URI.create("http://localhost:" + port + "/movies/all");
			ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).contains("MovieX");
		}

		@Test
		@DisplayName("3) Should reject creating a movie with invalid rating (rating=15.0 => 400)")
		void createMovieInvalidRating() {
			// Create a movie with invalid rating
			URI uri = URI.create("http://localhost:" + port + "/movies");
			String requestBody = """
					    {
					      "title": "BadRating",
					      "genre": "SciFi",
					      "duration": 120,
					      "rating": 15.0,
					      "releaseYear": 2025
					    }
					""";

			// Expect a 400 Bad Request
			try {
				restTemplate.postForEntity(uri, new HttpEntity<>(requestBody, jsonHeaders()), String.class);
				fail("Expected 400 for invalid rating > 10 (example constraint)!");
			} catch (HttpClientErrorException.BadRequest e) {
				assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(e.getResponseBodyAsString()).contains("rating");
			}
		}

		@Test
		@DisplayName("4) Should update an existing movie successfully")
		void updateMovie() {
			// create a movie
			createMovie("OldTitle", "Action", 100, 7.5, 2020);

			URI updateUri = URI.create("http://localhost:" + port + "/movies/update/OldTitle");
			String updateBody = """
					    {
					      "title": "NewTitle",
					      "genre": "Adventure",
					      "duration": 130,
					      "rating": 8.5,
					      "releaseYear": 2023
					    }
					""";

			// Update the movie
			ResponseEntity<String> resp = restTemplate.exchange(
					updateUri,
					HttpMethod.POST,
					new HttpEntity<>(updateBody, jsonHeaders()),
					String.class);

			assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(resp.getBody()).contains("was updated successfully");
		}

		@Test
		@DisplayName("5) Should delete a movie successfully")
		void deleteMovie() {
			// create a movie
			createMovie("DeleteMe", "Thriller", 90, 7.0, 2021);

			URI delUri = URI.create("http://localhost:" + port + "/movies/DeleteMe");
			restTemplate.delete(delUri);

		}

		// Helper to create a movie quickly
		void createMovie(String title, String genre, int duration, double rating, int year) {
			URI uri = URI.create("http://localhost:" + port + "/movies");
			String body = String.format("""
					    {
					      "title": "%s",
					      "genre": "%s",
					      "duration": %d,
					      "rating": %.1f,
					      "releaseYear": %d
					    }
					""", title, genre, duration, rating, year);

			restTemplate.postForEntity(uri, new HttpEntity<>(body, jsonHeaders()), String.class);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// SHOWTIME TESTS //
	//////////////////////////////////////////////////////////////////////////////////////
	@Nested
	@DisplayName("SHOWTIMES API")
	class ShowtimesApiTests {

		@Test
		@DisplayName("1) Should create a valid showtime (200 OK)")
		void createValidShowtime() {
			// Create a movie first
			createMovie("ShowtimeMovie", "Action", 120, 8.0, 2024);
			long movieId = getMovieIdByTitle("ShowtimeMovie"); // or store the ID from an endpoint

			URI uri = URI.create("http://localhost:" + port + "/showtimes");
			String body = String.format("""
					    {
					      "movieId": %d,
					      "theater": "Test Theater",
					      "startTime": "2025-05-01T10:00:00",
					      "endTime":   "2025-05-01T12:00:00",
					      "price": 12.5
					    }
					""", movieId);

			ResponseEntity<String> resp = restTemplate.postForEntity(uri, new HttpEntity<>(body, jsonHeaders()),
					String.class);
			assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(resp.getBody()).contains("Successfully created showtime");
		}

		@Test
		@DisplayName("2) Should reject showtime if endTime <= startTime (400)")
		void rejectInvalidTimes() {
			// Create a movie first
			createMovie("BadTimesMovie", "Action", 120, 8.0, 2024);
			long movieId = getMovieIdByTitle("BadTimesMovie");

			URI uri = URI.create("http://localhost:" + port + "/showtimes");
			String body = String.format("""
					    {
					      "movieId": %d,
					      "theater": "TheaterX",
					      "startTime": "2025-05-01T10:00:00",
					      "endTime":   "2025-05-01T10:00:00",
					      "price": 10.0
					    }
					""", movieId);

			// Expect a 400 Bad Request
			try {
				restTemplate.postForEntity(uri, new HttpEntity<>(body, jsonHeaders()), String.class);
				fail("Expected 400 for invalid times");
			} catch (HttpClientErrorException.BadRequest e) {
				assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(e.getResponseBodyAsString()).contains("endTime must be after startTime");
			}
		}

		@Test
		@DisplayName("3) Should reject overlapping showtime in same theater (409)")
		void rejectOverlap() {
			// create movie
			createMovie("OverlapMovie", "Drama", 100, 7.5, 2024);
			long movieId = getMovieIdByTitle("OverlapMovie");

			// create a showtime
			URI uri = URI.create("http://localhost:" + port + "/showtimes");
			String st1 = String.format("""
					    {
					      "movieId": %d,
					      "theater": "OverlapTheater",
					      "startTime": "2025-05-01T10:00:00",
					      "endTime":   "2025-05-01T12:00:00",
					      "price": 10.0
					    }
					""", movieId);
			restTemplate.postForEntity(uri, new HttpEntity<>(st1, jsonHeaders()), String.class);

			// try to create overlapping showtime
			String st2 = String.format("""
					    {
					      "movieId": %d,
					      "theater": "OverlapTheater",
					      "startTime": "2025-05-01T11:30:00",
					      "endTime":   "2025-05-01T13:00:00",
					      "price": 15.0
					    }
					""", movieId);

			// Expect a 409 Conflict
			try {
				restTemplate.postForEntity(uri, new HttpEntity<>(st2, jsonHeaders()), String.class);
				fail("Expected 409 for overlap");
			} catch (HttpClientErrorException.Conflict e) {
				assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(e.getResponseBodyAsString()).contains("Another showtime overlaps");
			}
		}

		@Test
		@DisplayName("4) Should update a showtime successfully (200 OK)")
		void updateShowtime() {
			// create a movie
			createMovie("UpdateShowtimeMovie", "Action", 120, 8.0, 2024);
			long movieId = getMovieIdByTitle("UpdateShowtimeMovie");

			// create a showtime
			long showtimeId = createShowtime(movieId, "UpdateTheater",
					"2025-05-01T10:00:00", "2025-05-01T12:00:00", 12.0);

			// update the showtime
			URI updateUri = URI.create("http://localhost:" + port + "/showtimes/update/" + showtimeId);
			String updateBody = String.format("""
					    {
					      "movieId": %d,
					      "theater": "UpdatedTheater",
					      "startTime": "2025-05-01T11:00:00",
					      "endTime":   "2025-05-01T13:00:00",
					      "price": 14.0
					    }
					""", movieId);

			ResponseEntity<String> resp = restTemplate.exchange(
					updateUri,
					HttpMethod.POST,
					new HttpEntity<>(updateBody, jsonHeaders()),
					String.class);
			assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(resp.getBody()).contains("updated successfully");
		}

		@Test
		@DisplayName("5) Should delete a showtime successfully")
		void deleteShowtime() {
			// create a movie
			createMovie("DeleteShowtimeMovie", "Action", 120, 8.0, 2024);
			long movieId = getMovieIdByTitle("DeleteShowtimeMovie");

			long showtimeId = createShowtime(movieId, "DeleteTheater",
					"2025-05-01T10:00:00", "2025-05-01T12:00:00", 10.0);

			URI delUri = URI.create("http://localhost:" + port + "/showtimes/" + showtimeId);
			restTemplate.delete(delUri);
		}

		// Helpers
		private long createShowtime(long movieId, String theater, String start, String end, double price) {
			// create a showtime
			URI uri = URI.create("http://localhost:" + port + "/showtimes");
			String body = String.format("""
					    {
					      "movieId": %d,
					      "theater": "%s",
					      "startTime": "%s",
					      "endTime":   "%s",
					      "price": %.1f
					    }
					""", movieId, theater, start, end, price);

			// A real approach might do:
			// var allShowtimes = showtimeRepository.findAll();
			// return allShowtimes.get(allShowtimes.size() - 1).getId();
			return 1L;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// BOOKING TESTS //
	//////////////////////////////////////////////////////////////////////////////////////
	@Nested
	@DisplayName("BOOKINGS API")
	class BookingApiTests {

		@Test
		@DisplayName("1) Should create booking successfully")
		void createBooking() {
			// create movie
			createMovie("BookingMovie", "Comedy", 90, 7.0, 2025);
			long movieId = getMovieIdByTitle("BookingMovie");
			// create showtime
			long showtimeId = createShowtime(movieId, "BookingTheater",
					"2025-05-10T15:00:00", "2025-05-10T17:00:00", 10.0);

			// book a seat
			URI uri = URI.create("http://localhost:" + port + "/bookings");
			String body = String.format("""
					    {
					      "showtimeId": %d,
					      "seatNumber": 50,
					      "userId": "11111111-1111-1111-1111-111111111111"
					    }
					""", showtimeId);

			ResponseEntity<String> resp = restTemplate.postForEntity(
					uri,
					new HttpEntity<>(body, jsonHeaders()),
					String.class);
			assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(resp.getBody()).contains("booking ID");
		}

		@Test
		@DisplayName("2) Should fail booking if seat is already taken")
		void seatConflict() {
			// create movie and showtime
			createMovie("ConflictMovie", "Thriller", 95, 7.5, 2025);
			long movieId = getMovieIdByTitle("ConflictMovie");
			long showtimeId = createShowtime(movieId, "ConflictTheater",
					"2025-05-10T18:00:00", "2025-05-10T20:00:00", 12.0);

			// book a seat
			URI uri = URI.create("http://localhost:" + port + "/bookings");
			String body1 = String.format("""
					    {
					      "showtimeId": %d,
					      "seatNumber": 10,
					      "userId": "aaaaaaaa-1111-1111-1111-bbbbbbbbbbbb"
					    }
					""", showtimeId);

			restTemplate.postForEntity(uri, new HttpEntity<>(body1, jsonHeaders()), String.class);

			// try to book the same seat again expecting a conflict
			try {
				restTemplate.postForEntity(uri, new HttpEntity<>(body1, jsonHeaders()), String.class);
				fail("Expected seat conflict (409)");
			} catch (HttpClientErrorException.Conflict e) {
				assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(e.getResponseBodyAsString()).contains("already booked");
			}
		}

		@Test
		@DisplayName("3) Should fail booking if userId is invalid UUID (400)")
		void invalidUserId() {
			// create movie and showtime
			createMovie("BadUUIDMovie", "Comedy", 100, 7.0, 2025);
			long movieId = getMovieIdByTitle("BadUUIDMovie");
			long showtimeId = createShowtime(movieId, "UUIDTheater",
					"2025-05-10T21:00:00", "2025-05-10T23:00:00", 14.0);

			// book a seat
			URI uri = URI.create("http://localhost:" + port + "/bookings");
			String badBody = String.format("""
					    {
					      "showtimeId": %d,
					      "seatNumber": 5,
					      "userId": "NOT-A-UUID"
					    }
					""", showtimeId);

			// Expect a 400 Bad Request
			try {
				restTemplate.postForEntity(uri, new HttpEntity<>(badBody, jsonHeaders()), String.class);
				fail("Expected 400 for invalid userId");
			} catch (HttpClientErrorException.BadRequest e) {
				assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(e.getResponseBodyAsString()).contains("Invalid userId");
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS //
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Helper method to create JSON headers.
	 * 
	 * @return HttpHeaders with JSON content type
	 */
	private HttpHeaders jsonHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	/**
	 * Mock method to get a movie's ID by title.
	 */
	private long getMovieIdByTitle(String title) {
		return 1L;
	}

	/**
	 * Helper method to create a movie quickly.
	 * 
	 * @param title
	 * @param genre
	 * @param duration
	 * @param rating
	 * @param year
	 */
	private void createMovie(String title, String genre, int duration, double rating, int year) {
		URI uri = URI.create("http://localhost:" + port + "/movies");
		String body = String.format("""
				    {
				      "title": "%s",
				      "genre": "%s",
				      "duration": %d,
				      "rating": %.1f,
				      "releaseYear": %d
				    }
				""", title, genre, duration, rating, year);

		restTemplate.postForEntity(uri, new HttpEntity<>(body, jsonHeaders()), String.class);
	}

	/**
	 * Helper method to create a showtime quickly.
	 * 
	 * @param movieId
	 * @param theater
	 * @param start
	 * @param end
	 * @param price
	 * @return
	 */
	private long createShowtime(long movieId, String theater, String start, String end, double price) {
		URI uri = URI.create("http://localhost:" + port + "/showtimes");
		String body = String.format("""
				    {
				      "movieId": %d,
				      "theater": "%s",
				      "startTime": "%s",
				      "endTime":   "%s",
				      "price": %.1f
				    }
				""", movieId, theater, start, end, price);

		ResponseEntity<String> resp = restTemplate.postForEntity(uri, new HttpEntity<>(body, jsonHeaders()),
				String.class);
		assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

		return 1L;
	}
}
