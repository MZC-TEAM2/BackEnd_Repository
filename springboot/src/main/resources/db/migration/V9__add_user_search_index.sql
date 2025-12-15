-- 유저 탐색 기능을 위한 인덱스 추가
-- 이름순 정렬 + 커서 기반 페이징 최적화

CREATE INDEX idx_user_profiles_name_user_id ON user_profiles(name, user_id);
