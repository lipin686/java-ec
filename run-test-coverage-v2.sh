#!/bin/bash

echo "======================================"
echo "🧪 執行 Docker 測試覆蓋率分析"
echo "======================================"

# 清理舊的測試容器和報告
echo "📦 清理舊的測試環境..."
docker-compose -f docker-compose.test.yml down -v 2>/dev/null
rm -rf spring-boot/target/site/jacoco 2>/dev/null
rm -rf spring-boot/target/surefire-reports 2>/dev/null

# 啟動測試（使用 --exit-code-from 確保獲得正確的退出碼）
echo "🚀 啟動測試容器..."
echo ""
docker-compose -f docker-compose.test.yml up --build --exit-code-from backend-test

TEST_EXIT_CODE=$?

echo ""
echo "📦 複製測試報告到本地..."
# 等待一秒讓容器完全停止
sleep 2

# 從容器複製測試報告
docker cp demo-test:/app/target/site spring-boot/target/ 2>/dev/null && echo "✅ 複製 jacoco 報告成功" || echo "⚠️  無法複製 jacoco 報告"
docker cp demo-test:/app/target/surefire-reports spring-boot/target/ 2>/dev/null && echo "✅ 複製測試報告成功" || echo "⚠️  無法複製測試報告"

# 清理測試容器
echo ""
echo "🧹 清理測試容器..."
docker-compose -f docker-compose.test.yml down 2>/dev/null

echo ""
echo "======================================"
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ 測試成功完成！"
else
    echo "⚠️  測試執行完成（有失敗）"
fi
echo "======================================"

# 顯示報告位置
if [ -f "spring-boot/target/site/jacoco/index.html" ]; then
    echo ""
    echo "📊 測試覆蓋率報告："
    echo "   HTML: spring-boot/target/site/jacoco/index.html"
    echo "   XML:  spring-boot/target/site/jacoco/jacoco.xml"
    echo ""
    echo "💡 打開報告："
    echo "   open spring-boot/target/site/jacoco/index.html"
    echo ""
fi

# 顯示測試結果摘要
if [ -d "spring-boot/target/surefire-reports" ]; then
    echo "📋 測試結果摘要："
    echo ""
    for file in spring-boot/target/surefire-reports/*.txt; do
        if [ -f "$file" ]; then
            echo "--- $(basename $file) ---"
            head -10 "$file"
            echo ""
        fi
    done
fi

echo "======================================"
echo "✨ 完成！"
echo "======================================"

exit $TEST_EXIT_CODE

