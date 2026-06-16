#!/usr/bin/env bash

# File kết quả đầu ra
OUTPUT_FILE="project_codebase.txt"

# Khởi tạo hoặc xóa file cũ
echo "===== PROJECT STRUCTURE =====" > "$OUTPUT_FILE"

# 1. Xuất cấu trúc thư mục (Bỏ qua target, git, ý tưởng)
if command -v tree &> /dev/null; then
    tree -I 'target|.git|.idea|.settings|.project|bin' >> "$OUTPUT_FILE"
else
    find . -maxdepth 4 -not -path '*/.*' -not -path './target*' -not -path './bin*' >> "$OUTPUT_FILE"
fi

echo -e "\n===== SOURCE CODE CONTENT =====\n" >> "$OUTPUT_FILE"

# 2. Duyệt qua tất cả các file cấu hình và code Java
# Bạn có thể thêm các đuôi file khác nếu cần (ví dụ: -name "*.yml" -o -name "*.properties")
find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.properties" \) \
    -not -path '*/.*' \
    -not -path './target/*' \
    -not -path './bin/*' | while read -r file; do

    echo "========================================" >> "$OUTPUT_FILE"
    echo "FILE: $file" >> "$OUTPUT_FILE"
    echo "========================================" >> "$OUTPUT_FILE"
    echo "---" >> "$OUTPUT_FILE"

    # Lấy đuôi file để format markdown tương ứng
    ext="${file##*.}"
    if [ "$ext" = "yml" ] || [ "$ext" = "properties" ]; then
        lang="yaml"
    else
        lang="java"
    fi

    echo "\`\`\`$lang" >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    echo -e "\n\`\`\`\n" >> "$OUTPUT_FILE"
done

echo "Đã xuất toàn bộ codebase thành công ra file: $OUTPUT_FILE"
