-- Insert sample data for testing

-- Insert colleges
INSERT INTO colleges (id, name, code) VALUES
(1, '공과대학', 'ENG'),
(2, '경영대학', 'BUS'),
(3, '인문대학', 'HUM');

-- Insert departments
INSERT INTO departments (id, college_id, name, code) VALUES
(1, 1, '컴퓨터공학과', 'CS'),
(2, 1, '전자공학과', 'EE'),
(3, 2, '경영학과', 'BA'),
(4, 2, '회계학과', 'ACC'),
(5, 3, '국문학과', 'KOR'),
(6, 3, '영문학과', 'ENG');