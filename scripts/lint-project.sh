#!/usr/bin/env bash
# =============================================================
# 博文学堂 项目 Linter（自反馈脚本）
# 用法: bash scripts/lint-project.sh
# 返回: 0 = 全部通过, 非0 = 有违规
# =============================================================

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC_DIR="$PROJECT_ROOT/src/main/java"
SQL_DIR="$PROJECT_ROOT/sql"
POM_FILE="$PROJECT_ROOT/pom.xml"

ERRORS=0
WARNINGS=0

# 颜色
RED='\033[0;31m'
YELLOW='\033[0;33m'
GREEN='\033[0;32m'
NC='\033[0m'

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    ERRORS=$((ERRORS + 1))
}

warn() {
    echo -e "${YELLOW}[WARN]  $1${NC}"
    WARNINGS=$((WARNINGS + 1))
}

pass() {
    echo -e "${GREEN}[PASS]  $1${NC}"
}

echo "=========================================="
echo " 博文学堂 Harness Linter"
echo "=========================================="
echo ""

# --- R1: javax 包引用检查 ---
echo "--- R1: javax 包引用检查 ---"
if [ -d "$SRC_DIR" ]; then
    JAVAX_HITS=$(grep -rn "^import javax\." "$SRC_DIR" 2>/dev/null || true)
    if [ -n "$JAVAX_HITS" ]; then
        error "R1: 发现 javax.* 导入（必须使用 jakarta.*）:"
        echo "$JAVAX_HITS" | head -10
    else
        pass "R1: 无 javax.* 引用"
    fi
else
    pass "R1: src 目录不存在，跳过"
fi

# --- R2: Spring Boot 版本锁定 ---
echo ""
echo "--- R2: Spring Boot 版本锁定 ---"
if [ -f "$POM_FILE" ]; then
    BOOT_VERSION=$(grep -A2 "spring-boot-starter-parent" "$POM_FILE" | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' || true)
    if [ -n "$BOOT_VERSION" ] && [ "$BOOT_VERSION" != "3.2.5" ]; then
        error "R2: Spring Boot 版本为 $BOOT_VERSION，必须为 3.2.5"
    elif [ -n "$BOOT_VERSION" ]; then
        pass "R2: Spring Boot 版本正确 (3.2.5)"
    else
        warn "R2: 未找到 spring-boot-starter-parent 版本声明"
    fi
else
    warn "R2: pom.xml 不存在，跳过版本检查"
fi

# --- R3: review_record 表 UPDATE/DELETE 检查 ---
echo ""
echo "--- R3: review_record 只读检查 ---"
if [ -d "$SRC_DIR" ]; then
    # 检查 Mapper/Service 中是否有对 review_record 的 update/delete 操作
    RR_MODIFY=$(grep -rn "review_record\|ReviewRecord" "$SRC_DIR" 2>/dev/null | grep -i "update\|delete\|remove" || true)
    if [ -n "$RR_MODIFY" ]; then
        error "R3: 发现对 review_record 的修改/删除操作:"
        echo "$RR_MODIFY" | head -5
    else
        pass "R3: review_record 无修改/删除操作"
    fi
else
    pass "R3: src 目录不存在，跳过"
fi

# --- R4: Controller 层业务逻辑检查 ---
echo ""
echo "--- R4: Controller 层复杂度检查 ---"
if [ -d "$SRC_DIR" ]; then
    CONTROLLER_DIR=$(find "$SRC_DIR" -type d -name "controller" 2>/dev/null)
    if [ -n "$CONTROLLER_DIR" ]; then
        # 检查 Controller 中是否有 @Transactional（不应出现）
        CTRL_TXN=$(grep -rn "@Transactional" $CONTROLLER_DIR 2>/dev/null || true)
        if [ -n "$CTRL_TXN" ]; then
            warn "R4: Controller 层发现 @Transactional（业务逻辑应在 Service 层）:"
            echo "$CTRL_TXN" | head -5
        else
            pass "R4: Controller 层无 @Transactional"
        fi
    else
        pass "R4: 无 controller 目录"
    fi
else
    pass "R4: src 目录不存在，跳过"
fi

# --- R5: 硬编码状态值检查 ---
echo ""
echo "--- R5: 硬编码状态值检查 ---"
if [ -d "$SRC_DIR" ]; then
    # 检查 Service/Controller 中是否有 status == 数字 或 setStatus(数字) 模式
    HARDCODE=$(grep -rn "setStatus([0-9])\|\.status\s*==\s*[0-9]\|getStatus()\s*==\s*[0-9]" "$SRC_DIR" 2>/dev/null | grep -v "enum\|Enum\|test\|Test" || true)
    if [ -n "$HARDCODE" ]; then
        warn "R5: 发现硬编码状态值（应使用枚举常量）:"
        echo "$HARDCODE" | head -5
    else
        pass "R5: 无硬编码状态值"
    fi
else
    pass "R5: src 目录不存在，跳过"
fi

# --- R6: 推荐数量上限校验 ---
echo ""
echo "--- R6: 推荐数量上限校验 ---"
if [ -d "$SRC_DIR" ]; then
    # 检查是否存在对推荐数量的校验逻辑
    REC_CHECK=$(grep -rn "5\|MAX_RECOMMEND\|maxRecommend" "$SRC_DIR" 2>/dev/null | grep -i "recommend\|推荐" || true)
    if [ -d "$SRC_DIR" ] && [ -n "$(find "$SRC_DIR" -name "*Recommend*" -o -name "*recommend*" 2>/dev/null)" ]; then
        if [ -z "$REC_CHECK" ]; then
            warn "R6: 存在推荐相关代码，但未发现数量上限校验（每条需求最多推荐5位）"
        else
            pass "R6: 推荐数量校验存在"
        fi
    else
        pass "R6: 暂无推荐相关代码"
    fi
else
    pass "R6: src 目录不存在，跳过"
fi

# --- R7: SQL 文件语法基础检查 ---
echo ""
echo "--- R7: SQL 文件检查 ---"
if [ -f "$SQL_DIR/schema.sql" ]; then
    # 检查是否使用了 utf8mb4
    if grep -q "utf8mb4" "$SQL_DIR/schema.sql"; then
        pass "R7: schema.sql 使用 utf8mb4 字符集"
    else
        warn "R7: schema.sql 未找到 utf8mb4 声明"
    fi
    # 检查是否所有表都是 InnoDB
    NON_INNODB=$(grep -i "ENGINE=" "$SQL_DIR/schema.sql" | grep -iv "InnoDB" || true)
    if [ -n "$NON_INNODB" ]; then
        error "R7: 发现非 InnoDB 引擎的表:"
        echo "$NON_INNODB"
    else
        pass "R7: 所有表均使用 InnoDB"
    fi
else
    warn "R7: sql/schema.sql 不存在"
fi

# --- R8: 单元测试必须通过 ---
echo ""
echo "--- R8: 单元测试 (mvn test) ---"
export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home}"
if "$PROJECT_ROOT/mvnw" test -f "$PROJECT_ROOT/pom.xml" -q 2>/dev/null; then
    pass "R8: mvn test 全部通过"
else
    error "R8: mvn test 失败，存在测试未通过"
fi

# --- R9: 对抗测试文档三件套完整性检查 ---
echo ""
echo "--- R9: 对抗测试文档完整性 ---"
TEST_DOC_DIR="$PROJECT_ROOT/docs/test"
MISSING_DOCS=0
for DOC in "adversarial-dataset.md" "test-plan.md" "coverage-map.md"; do
    if [ ! -f "$TEST_DOC_DIR/$DOC" ]; then
        error "R9: 缺少测试文档 docs/test/$DOC"
        MISSING_DOCS=1
    fi
done
if [ $MISSING_DOCS -eq 0 ]; then
    # 检查三件套是否包含所有已存在的 ServiceImpl 对应章节
    if [ -d "$SRC_DIR" ]; then
        SERVICE_IMPLS=$(find "$SRC_DIR" -name "*ServiceImpl.java" -exec basename {} .java \; 2>/dev/null | sort)
        DATASET_CONTENT=$(cat "$TEST_DOC_DIR/adversarial-dataset.md" 2>/dev/null)
        UNCOVERED=""
        for SVC in $SERVICE_IMPLS; do
            # 从 XxxServiceImpl 提取 Xxx 作为关键词
            SVC_NAME=$(echo "$SVC" | sed 's/ServiceImpl$//')
            if ! echo "$DATASET_CONTENT" | grep -qi "$SVC_NAME"; then
                UNCOVERED="$UNCOVERED $SVC_NAME"
            fi
        done
        if [ -n "$UNCOVERED" ]; then
            warn "R9: 以下 Service 在对抗数据集中未找到对应章节:$UNCOVERED"
        else
            pass "R9: 对抗测试文档三件套完整，所有 Service 已覆盖"
        fi
    else
        pass "R9: 对抗测试文档三件套存在"
    fi
fi

# --- 汇总 ---
echo ""
echo "=========================================="
echo " 检查完成"
echo "=========================================="
echo -e " ERRORS:   ${RED}${ERRORS}${NC}"
echo -e " WARNINGS: ${YELLOW}${WARNINGS}${NC}"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}❌ 有 $ERRORS 个 ERROR，必须修复后才能继续${NC}"
    exit 1
else
    echo -e "${GREEN}✅ 无 ERROR（$WARNINGS 个 WARNING 建议关注）${NC}"
    exit 0
fi
