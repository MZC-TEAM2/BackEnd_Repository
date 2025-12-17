-- -----------------------------------------------------
-- V13: 댓글 알림 타입 추가
-- -----------------------------------------------------

INSERT INTO notification_types (type_code, type_name, category, default_message_template, is_active) VALUES
('COMMENT_CREATED', '댓글 등록', '게시판', '내 게시글에 새 댓글이 등록되었습니다.', true),
('REPLY_CREATED', '대댓글 등록', '게시판', '내 댓글에 답글이 등록되었습니다.', true);