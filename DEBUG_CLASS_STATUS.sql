-- Kiểm tra status của tất cả classes
SELECT
    c.id,
    c.class_code,
    c.class_name,
    co.name as course_name,
    c.status,
    c.current_students,
    c.max_students,
    CASE
        WHEN c.current_students < c.max_students THEN 'Còn chỗ'
        ELSE 'Đã đầy'
    END as availability
FROM classes c
JOIN courses co ON c.course_id = co.id
ORDER BY c.status, c.id;

-- Đếm theo status
SELECT status, COUNT(*) as count
FROM classes
GROUP BY status;

-- Kiểm tra lớp nào thỏa điều kiện hiện tại (PENDING và còn chỗ)
SELECT
    c.id,
    c.class_code,
    c.class_name,
    c.status,
    c.current_students,
    c.max_students
FROM classes c
WHERE c.status = 'PENDING'
  AND c.current_students < c.max_students;
