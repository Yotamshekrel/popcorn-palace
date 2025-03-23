INSERT INTO task ( description, completed) VALUES
                                               ( 'description1', 'completed'),
                                               ('description2', 'completed');

-- Insert sample movies into the movies table
INSERT INTO movies (title, genre, duration, rating, release_year) VALUES 
    ('The Dark Knight', 'Action', 152, 9.0, 2008),
    ('Inception', 'Sci-Fi', 148, 8.8, 2010),
    ('Interstellar', 'Sci-Fi', 169, 8.6, 2014),
    ('The Godfather', 'Crime', 175, 9.2, 1972),
    ('Titanic', 'Romance', 195, 7.8, 1997);

-- Insert sample showtimes into the showtimes table
INSERT INTO showtimes (movie_id, theater, start_time, end_time, price) VALUES
    (1, 'IMAX Theater', '2025-03-21 14:00:00', '2025-03-21 16:30:00', 15.00),
    (1, 'Cineplex 10', '2025-03-22 19:00:00', '2025-03-22 21:30:00', 12.50),
    (2, 'City Cinema', '2025-03-23 17:00:00', '2025-03-23 19:30:00', 13.00),
    (3, 'Grand Theater', '2025-03-24 20:00:00', '2025-03-24 23:00:00', 18.00),
    (4, 'Retro Cinema', '2025-03-25 15:30:00', '2025-03-25 18:00:00', 10.00);

-- Insert sample bookings into the bookings table
INSERT INTO bookings (id, showtime_id, user_id, seat_number, booking_time) VALUES
    ('11111111-aaaa-4bbb-cccc-111111111111', 1, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 5, '2025-03-20 10:00:00'),
    ('22222222-bbbb-4ccc-dddd-222222222222', 1, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 12, '2025-03-20 10:05:00'),
    ('33333333-cccc-4ddd-eeee-333333333333', 2, 'cccccccc-cccc-cccc-cccc-cccccccccccc', 8, '2025-03-21 15:00:00'),
    ('44444444-dddd-4eee-ffff-444444444444', 3, 'dddddddd-dddd-dddd-dddd-dddddddddddd', 22, '2025-03-22 18:00:00'),
    ('55555555-eeee-4fff-aaaa-555555555555', 4, 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 7, '2025-03-23 16:30:00');
