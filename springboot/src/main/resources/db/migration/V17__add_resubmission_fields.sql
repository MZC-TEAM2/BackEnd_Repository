-- 재제출 관련 컬럼 추가
ALTER TABLE assignment_submissions
    ADD COLUMN allow_resubmission BOOLEAN DEFAULT FALSE COMMENT '재제출 허용 여부',
    ADD COLUMN resubmission_deadline DATETIME(6) COMMENT '재제출 마감일';

-- 기존 데이터에 대한 기본값 설정
UPDATE assignment_submissions
SET allow_resubmission = FALSE
WHERE allow_resubmission IS NULL;
