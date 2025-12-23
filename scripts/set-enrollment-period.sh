#!/bin/bash
#
#  # 현재 상태 확인
 #  ./scripts/set-enrollment-period.sh status
 #
 #  # 1학년 수강신청만 활성화
 #  ./scripts/set-enrollment-period.sh 1
 #
 #  # 강의등록 기간만 활성화
 #  ./scripts/set-enrollment-period.sh course-reg
 #
 #  # 모든 기간 활성화
 #  ./scripts/set-enrollment-period.sh all
 #
 #  # 모든 기간 비활성화
 #  ./scripts/set-enrollment-period.sh none
#

# 수강신청 기간 설정 스크립트
# 사용법: ./set-enrollment-period.sh [옵션]
#
# 옵션:
#   1, 2, 3, 4       - 해당 학년 수강신청 기간만 활성화
#   enrollment       - 전학년 수강신청 기간 활성화
#   course-reg       - 강의등록 기간만 활성화
#   adjustment       - 변경 기간만 활성화
#   cancellation     - 취소 기간만 활성화
#   all              - 모든 기간 활성화
#   none             - 모든 기간 비활성화 (미래로 설정)
#   status           - 현재 상태 확인

DB_USER="${DB_USER:-lmsuser}"
DB_PASS="${DB_PASS:-lmspassword}"
DB_NAME="${DB_NAME:-lms_db}"
DB_HOST="${DB_HOST:-lms-mysql}"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# MySQL 실행 함수
run_sql() {
    docker exec $DB_HOST mysql -u$DB_USER -p$DB_PASS $DB_NAME -N -e "$1" 2>/dev/null
}

# 현재 시간 (UTC)
NOW=$(run_sql "SELECT NOW();")
TODAY=$(run_sql "SELECT DATE(NOW());")

# 활성 기간 (오늘 00:00 ~ 내일 18:00)
ACTIVE_START="${TODAY} 00:00:00"
ACTIVE_END=$(run_sql "SELECT DATE_ADD('${TODAY}', INTERVAL 1 DAY);")
ACTIVE_END="${ACTIVE_END} 18:00:00"

# 비활성 기간 (한달 후)
INACTIVE_START=$(run_sql "SELECT DATE_ADD('${TODAY}', INTERVAL 1 MONTH);")
INACTIVE_END=$(run_sql "SELECT DATE_ADD('${TODAY}', INTERVAL 1 MONTH) + INTERVAL 1 DAY;")
INACTIVE_START="${INACTIVE_START} 09:00:00"
INACTIVE_END="${INACTIVE_END} 18:00:00"

show_status() {
    echo -e "${BLUE}=== 현재 수강신청 기간 상태 ===${NC}"
    echo -e "서버 시간: ${YELLOW}${NOW}${NC}"
    echo ""

    # 기간 유형 정보
    echo -e "${GREEN}[활성화된 기간]${NC}"
    run_sql "
    SELECT CONCAT(
        CASE pt.type_code
            WHEN 'ENROLLMENT' THEN '수강신청'
            WHEN 'COURSE_REGISTRATION' THEN '강의등록'
            WHEN 'ADJUSTMENT' THEN '변경'
            WHEN 'CANCELLATION' THEN '취소'
        END,
        CASE WHEN ep.target_year > 0 THEN CONCAT(' (', ep.target_year, '학년)') ELSE '' END,
        ': ', DATE_FORMAT(ep.start_datetime, '%m/%d %H:%i'), ' ~ ', DATE_FORMAT(ep.end_datetime, '%m/%d %H:%i')
    ) as info
    FROM enrollment_periods ep
    JOIN period_types pt ON ep.period_type_id = pt.id
    WHERE ep.start_datetime <= NOW() AND ep.end_datetime >= NOW()
    ORDER BY pt.id, ep.target_year;"

    echo ""
    echo -e "${YELLOW}[전체 기간 목록]${NC}"
    run_sql "
    SELECT CONCAT(
        LPAD(ep.id, 2, ' '), ' | ',
        CASE pt.type_code
            WHEN 'ENROLLMENT' THEN '수강신청'
            WHEN 'COURSE_REGISTRATION' THEN '강의등록'
            WHEN 'ADJUSTMENT' THEN '변경    '
            WHEN 'CANCELLATION' THEN '취소    '
        END,
        ' | ',
        CASE WHEN ep.target_year > 0 THEN CONCAT(ep.target_year, '학년') ELSE '전체' END,
        ' | ',
        DATE_FORMAT(ep.start_datetime, '%m/%d %H:%i'), ' ~ ', DATE_FORMAT(ep.end_datetime, '%m/%d %H:%i'),
        ' | ',
        CASE WHEN ep.start_datetime <= NOW() AND ep.end_datetime >= NOW() THEN '● 활성' ELSE '○ 비활성' END
    ) as info
    FROM enrollment_periods ep
    JOIN period_types pt ON ep.period_type_id = pt.id
    ORDER BY pt.id, ep.target_year;"
}

set_all_inactive() {
    echo -e "${YELLOW}모든 기간을 비활성화합니다...${NC}"
    run_sql "UPDATE enrollment_periods SET start_datetime = '${INACTIVE_START}', end_datetime = '${INACTIVE_END}';"
}

activate_by_year() {
    local year=$1
    echo -e "${GREEN}${year}학년 수강신청 기간을 활성화합니다...${NC}"
    set_all_inactive
    run_sql "UPDATE enrollment_periods SET start_datetime = '${ACTIVE_START}', end_datetime = '${ACTIVE_END}' WHERE period_type_id = 1 AND target_year = ${year};"
}

activate_enrollment_all() {
    echo -e "${GREEN}전학년 수강신청 기간을 활성화합니다...${NC}"
    set_all_inactive
    run_sql "UPDATE enrollment_periods SET start_datetime = '${ACTIVE_START}', end_datetime = '${ACTIVE_END}' WHERE period_type_id = 1;"
}

activate_course_registration() {
    echo -e "${GREEN}강의등록 기간을 활성화합니다...${NC}"
    set_all_inactive
    run_sql "UPDATE enrollment_periods SET start_datetime = '${ACTIVE_START}', end_datetime = '${ACTIVE_END}' WHERE period_type_id = 2;"
}

activate_adjustment() {
    echo -e "${GREEN}변경 기간을 활성화합니다...${NC}"
    set_all_inactive
    run_sql "UPDATE enrollment_periods SET start_datetime = '${ACTIVE_START}', end_datetime = '${ACTIVE_END}' WHERE period_type_id = 3;"
}

activate_cancellation() {
    echo -e "${GREEN}취소 기간을 활성화합니다...${NC}"
    set_all_inactive
    run_sql "UPDATE enrollment_periods SET start_datetime = '${ACTIVE_START}', end_datetime = '${ACTIVE_END}' WHERE period_type_id = 4;"
}

activate_all() {
    echo -e "${GREEN}모든 기간을 활성화합니다...${NC}"
    run_sql "UPDATE enrollment_periods SET start_datetime = '${ACTIVE_START}', end_datetime = '${ACTIVE_END}';"
}

show_help() {
    echo -e "${BLUE}수강신청 기간 설정 스크립트${NC}"
    echo ""
    echo "사용법: $0 [옵션]"
    echo ""
    echo "옵션:"
    echo "  1, 2, 3, 4       해당 학년 수강신청 기간만 활성화"
    echo "  enrollment       전학년 수강신청 기간 활성화"
    echo "  course-reg       강의등록 기간만 활성화"
    echo "  adjustment       변경 기간만 활성화"
    echo "  cancellation     취소 기간만 활성화"
    echo "  all              모든 기간 활성화"
    echo "  none             모든 기간 비활성화"
    echo "  status           현재 상태 확인"
    echo ""
    echo "예시:"
    echo "  $0 1             # 1학년 수강신청만 활성화"
    echo "  $0 course-reg    # 강의등록 기간만 활성화"
    echo "  $0 status        # 현재 상태 확인"
}

# 메인 로직
case "$1" in
    1|2|3|4)
        activate_by_year $1
        echo ""
        show_status
        ;;
    enrollment)
        activate_enrollment_all
        echo ""
        show_status
        ;;
    course-reg|course_reg|course-registration)
        activate_course_registration
        echo ""
        show_status
        ;;
    adjustment|adjust)
        activate_adjustment
        echo ""
        show_status
        ;;
    cancellation|cancel)
        activate_cancellation
        echo ""
        show_status
        ;;
    all)
        activate_all
        echo ""
        show_status
        ;;
    none|off)
        set_all_inactive
        echo ""
        show_status
        ;;
    status|stat|s)
        show_status
        ;;
    -h|--help|help)
        show_help
        ;;
    *)
        if [ -z "$1" ]; then
            show_status
        else
            echo -e "${RED}알 수 없는 옵션: $1${NC}"
            echo ""
            show_help
            exit 1
        fi
        ;;
esac
