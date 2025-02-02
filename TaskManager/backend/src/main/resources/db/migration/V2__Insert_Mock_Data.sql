
INSERT INTO app_user (id, username, password, role)
VALUES
    (1, 'manager_john', '$2y$12$aTCy.mBS/./y1CSgOwFC6uvpETIGBBp1p/ik.v1HJbqoxyyKfHD6u', 'MANAGER'),
    (2, 'manager_jane', '$2y$12$6NYSDUxBzhGmg6HlQ.kW.ecv0LZiiMzQG081oix/165U/8y1hCjcS', 'MANAGER'),
    (3, 'manager_bob', '$2y$12$ZuaESX8W93YNGr/QcvMM9.cbNO.6ng8MLnSZAKB05v3WIjtSKdQtm', 'MANAGER');

INSERT INTO app_user (id, username, password, role)
VALUES
    (4, 'team_leader_dev', '$2y$12$vq1azypWTfU48whytUl24.JWEz04jigcM0NBHhrvQ0W9FYRVo7ps6', 'USER'),
    (5, 'team_leader_qa', '$2y$12$qNNdkMsAOiTF.NX/bzoe9OlYHEM8r75KMa9FWYa6wX8PYEr5f14i.', 'USER'),
    (6, 'team_leader_design', '$2y$12$1wE2XzD9jcYMXgTDonEGp.FVB0pt.Ygw.F.jfYR7JUke8aDbwOUjm', 'USER'),
    (7, 'dev_member_1', '$2y$12$.OdB0m9Veci34EOwfHX3uuXIG6qn6c2ygEhK6F19nCsK8S4mhKhhy', 'USER'),
    (8, 'dev_member_2', '$2y$12$gxd5DHZ7qUrOIapNfoaDaeoKjxqGVLNZyPRbZhBPmLnm/VXClRdyK', 'USER'),
    (9, 'qa_member_1', '$2y$12$.5CPZI021y3UrpddrimJ6.UEopL5N1IM6Whmi/hE8j.ZULSgI3MD6', 'USER'),
    (10, 'qa_member_2', '$2y$12$ymO2XFK/aEfq.vvY4O6xXepK.ol3EG8keJPt4MyKbVMffEX.IvCP.', 'USER'),
    (11, 'design_member_1', '$2y$12$wleUBBay/CS2fUg8xOyJYO8Fk.Z5xIDbLTabjPDcgVJdm2s1oBkRm', 'USER'),
    (12, 'design_member_2', '$2y$12$YUWsgNHcYKAFhVlpI2Dgg.M38hseo/9/5O4Pa5I3OqJS8opMQNGQG', 'USER');
-- Insert admin data into app_user
INSERT INTO app_user (id, username, password, role)
VALUES
    (13, 'admin', '$2y$12$ufNl9SbD1LqcJuSBdRbc9OksnYMH842lSo6KS4We.8QZ1E/4PjOQy', 'ADMIN');

INSERT INTO users (id, name, surname, email, app_user_id)
VALUES
    (1, 'John', 'Manager', 'john.manager@example.com', 1),
    (2, 'Jane', 'Manager', 'jane.manager@example.com', 2),
    (3, 'Bob', 'Manager', 'bob.manager@example.com', 3),
    (4, 'Alice', 'Dev Leader', 'alice.dev@example.com', 4),
    (5, 'Carol', 'QA Leader', 'carol.qa@example.com', 5),
    (6, 'Eve', 'Design Leader', 'eve.design@example.com', 6),
    (7, 'Dev', 'Member1', 'dev1@example.com', 7),
    (8, 'Dev', 'Member2', 'dev2@example.com', 8),
    (9, 'QA', 'Member1', 'qa1@example.com', 9),
    (10, 'QA', 'Member2', 'qa2@example.com', 10),
    (11, 'Design', 'Member1', 'design1@example.com', 11),
    (12, 'Design', 'Member2', 'design2@example.com', 12),
    (13, 'admin', 'admin', 'admin@example.com', 13);

-- Insert teams into the `teams` table
INSERT INTO teams (id, title, created_date, created_by)
VALUES
    (1, 'Development Team', '2024-01-01', 1), -- Created by manager_john
    (2, 'QA Team', '2024-02-01', 2),         -- Created by manager_jane
    (3, 'Design Team', '2024-03-01', 3);     -- Created by manager_bob

-- Insert mock data into priority
INSERT INTO priority (id, name)
VALUES
    (1, 'High'),
    (2, 'Medium'),
    (3, 'Low');

-- Insert mock data into roles
INSERT INTO roles (id, name)
VALUES
    (1, 'Team Leader'),
    (2, 'Developer'),
    (3, 'Tester');
-- Insert mock data into task_status
INSERT INTO task_status (id, name)
VALUES
    (1, 'Pending'),
    (2, 'In Progress'),
    (3, 'Completed');

-- Insert user roles into the `user_team` table
-- Each team has one Team Leader and other members
INSERT INTO user_team (user_id, team_id, role_id, joined_date)
VALUES
    -- Development Team
    (4, 1, 1, '2024-01-05'), -- Alice as Team Leader
    (7, 1, 2, '2024-01-06'), -- Dev Member 1
    (8, 1, 2, '2024-01-07'), -- Dev Member 2

    -- QA Team
    (5, 2, 1, '2024-02-05'), -- Carol as Team Leader
    (9, 2, 3, '2024-02-06'), -- QA Member 1
    (10, 2, 3, '2024-02-07'), -- QA Member 2

    -- Design Team
    (6, 3, 1, '2024-03-05'), -- Eve as Team Leader
    (11, 3, 2, '2024-03-06'), -- Design Member 1
    (12, 3, 3, '2024-03-07'); -- Design Member 2

-- Insert tasks for Development Team (Created by manager_john, Team Leader: Alice)
INSERT INTO tasks (assigned_to_user_id, assigned_to_team_id, assigned_by_user_id, assigned_by_team_id, priority_id, status_id, title, description, assigned_date, due_date)
VALUES
    (7, 1, 1, 1, 1, 1, 'Setup CI/CD Pipeline', 'Configure Jenkins for CI/CD', '2024-01-10', '2024-01-20'), -- Assigned by manager_john
    (8, 1, 1, 1, 2, 2, 'Optimize Database Queries', 'Improve DB query performance', '2024-01-12', '2024-01-22'), -- Assigned by manager_john
    (7, 1, 4, 1, 3, 3, 'Write Unit Tests', 'Add unit tests for core modules', '2024-01-15', '2024-01-25'), -- Assigned by Alice
    (8, 1, 4, 1, 1, 2, 'Refactor Legacy Code', 'Improve readability of legacy modules', '2024-01-18', '2024-01-28'); -- Assigned by Alice

-- Insert tasks for QA Team (Created by manager_jane, Team Leader: Carol)
INSERT INTO tasks (assigned_to_user_id, assigned_to_team_id, assigned_by_user_id, assigned_by_team_id, priority_id, status_id, title, description, assigned_date, due_date)
VALUES
    (9, 2, 2, 2, 2, 1, 'Automate Regression Testing', 'Add scripts for regression tests', '2024-02-10', '2024-02-20'), -- Assigned by manager_jane
    (10, 2, 2, 2, 3, 2, 'Test API Endpoints', 'Validate API functionality', '2024-02-12', '2024-02-22'), -- Assigned by manager_jane
    (9, 2, 5, 2, 1, 3, 'Verify Security Vulnerabilities', 'Conduct penetration testing', '2024-02-15', '2024-02-25'), -- Assigned by Carol
    (10, 2, 5, 2, 2, 1, 'Cross-Browser Testing', 'Ensure compatibility with major browsers', '2024-02-18', '2024-02-28'); -- Assigned by Carol

-- Insert tasks for Design Team (Created by manager_bob, Team Leader: Eve)
INSERT INTO tasks (assigned_to_user_id, assigned_to_team_id, assigned_by_user_id, assigned_by_team_id, priority_id, status_id, title, description, assigned_date, due_date)
VALUES
    (11, 3, 3, 3, 1, 1, 'Create UI Mockups', 'Design mockups for new features', '2024-03-10', '2024-03-20'), -- Assigned by manager_bob
    (12, 3, 3, 3, 2, 2, 'Enhance Mobile Responsiveness', 'Improve design for smaller screens', '2024-03-12', '2024-03-22'), -- Assigned by manager_bob
    (11, 3, 6, 3, 3, 3, 'Develop Design Guidelines', 'Document style and branding rules', '2024-03-15', '2024-03-25'), -- Assigned by Eve
    (12, 3, 6, 3, 1, 2, 'Create SVG Assets', 'Produce vector graphics for the website', '2024-03-18', '2024-03-28'); -- Assigned by Eve

ALTER TABLE app_user ALTER COLUMN id RESTART WITH 14;

-- priority
ALTER TABLE priority ALTER COLUMN id RESTART WITH 4;

-- roles
ALTER TABLE roles ALTER COLUMN id RESTART WITH 4;

-- task_status
ALTER TABLE task_status ALTER COLUMN id RESTART WITH 4;

-- teams
ALTER TABLE teams ALTER COLUMN id RESTART WITH 4;

-- users
ALTER TABLE users ALTER COLUMN id RESTART WITH 14;

-- tasks
ALTER TABLE tasks ALTER COLUMN id RESTART WITH 13;