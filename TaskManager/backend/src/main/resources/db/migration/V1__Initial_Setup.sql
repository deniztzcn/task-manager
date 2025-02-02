CREATE TABLE app_user
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    role     VARCHAR(20) NOT NULL
);

CREATE TABLE priority
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE roles
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE task_status
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);


CREATE TABLE users
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    surname     VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    app_user_id INT          NOT NULL,
    CONSTRAINT fk_users_app_user FOREIGN KEY (app_user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE teams
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(100) NOT NULL,
    created_date DATE         NOT NULL,
    created_by INT            NOT NULL,
    CONSTRAINT fk_user_teams FOREIGN KEY (created_by) references users(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE user_team
(
    user_id     INT  NOT NULL,
    team_id     INT  NOT NULL,
    role_id     INT  NOT NULL,
    joined_date DATE NOT NULL,
    CONSTRAINT pk_user_team PRIMARY KEY (user_id, team_id),
    CONSTRAINT fk_user_team_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_team_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE tasks
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    assigned_to_user_id   INT NOT NULL,
    assigned_to_team_id   INT DEFAULT NULL,
    assigned_by_user_id   INT NOT NULL,
    assigned_by_team_id   INT DEFAULT NULL,
    priority_id   INT          NOT NULL,
    status_id     INT          NOT NULL,
    title         VARCHAR(100) NOT NULL,
    description   TEXT         NOT NULL,
    assigned_date DATE         NOT NULL,
    due_date      DATE         NOT NULL,
    CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to_user_id, assigned_to_team_id)
        REFERENCES user_team (user_id, team_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assigned_by FOREIGN KEY (assigned_by_user_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_tasks_priority FOREIGN KEY (priority_id) REFERENCES priority (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_tasks_status FOREIGN KEY (status_id) REFERENCES task_status (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);