CREATE TABLE IF NOT EXISTS task (
                                    description VARCHAR(64) NOT NULL,
    completed   VARCHAR(30) NOT NULL);

DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS showtimes CASCADE;
DROP TABLE IF EXISTS movies CASCADE;

-- Create Movies Table
CREATE TABLE IF NOT EXISTS movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    duration INT NOT NULL CHECK (duration > 0), -- Ensure positive duration
    rating DECIMAL(3,1) CHECK (rating BETWEEN 0 AND 10), -- Ensure valid rating
    release_year INT CHECK (release_year >= 1888) -- First movie was in 1888
);

-- Create Showtimes Table
CREATE TABLE IF NOT EXISTS showtimes (
    id SERIAL PRIMARY KEY,
    movie_id INT NOT NULL,
    theater VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    price DECIMAL(6,2) NOT NULL CHECK (price >= 0), -- Ensure valid price
    CONSTRAINT fk_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT chk_valid_time CHECK (end_time > start_time) -- Ensure valid showtimes
);

-- Create Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY,
    showtime_id INT NOT NULL,
    user_id UUID NOT NULL,
    seat_number INT NOT NULL CHECK (seat_number > 0),
    booking_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE,
    CONSTRAINT unique_seat_booking UNIQUE (showtime_id, seat_number)
);

