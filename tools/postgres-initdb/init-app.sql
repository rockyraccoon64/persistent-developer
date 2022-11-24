CREATE TYPE dev_state_type AS ENUM ('Free', 'Working', 'Resting');

CREATE TABLE dev_state(
	id VARCHAR(100) PRIMARY KEY,
	state dev_state_type NOT NULL
);

CREATE TYPE task_status AS ENUM ('Queued', 'InProgress', 'Finished');

CREATE TABLE task(
	uuid UUID PRIMARY KEY,
	created_at TIMESTAMP NOT NULL DEFAULT NOW(),
	difficulty INT NOT NULL,
	status task_status NOT NULL
);