-- 插入參考使用者 (ID 2000)
INSERT INTO members (id, name, email, occupation, level, exp, coin, created_at)
VALUES (2000, '導師 (範本帳號)', 'ref_mentor@test.com', 'Mentor', 99, 999999, 999999, NOW())
ON CONFLICT (id) DO NOTHING;

-- 為 JS 課程道館插入參考評語 (每關兩筆)
-- 601: 起點之石
INSERT INTO gym_challenge_records (user_id, gym_id, gym_challenge_id, status, feedback, ratings, created_at, completed_at, reviewed_at)
VALUES 
(2000, 601, 1, 'SUCCESS', '## 導師的評價\n\n變數宣告非常正確，使用了 let 和 const 的區分。建議在變數命名上可以更具語意化。', '{"1": "S", "2": "A", "3": "S", "4": "S"}', NOW(), NOW(), NOW()),
(2000, 601, 1, 'SUCCESS', '## 導師 Code Review\n\n基礎紮實！資料型態轉換的部分處理得很好。可以嘗試在註解中說明為什麼選擇某種資料型態。', '{"1": "SSS", "2": "SSS", "3": "S", "4": "A"}', NOW(), NOW(), NOW());

-- 602: 流程之境
INSERT INTO gym_challenge_records (user_id, gym_id, gym_challenge_id, status, feedback, ratings, created_at, completed_at, reviewed_at)
VALUES 
(2000, 602, 1, 'SUCCESS', '## 導師的評價\n\nif/else 邏輯清晰，邊界條件都有考慮到。建議可以嘗試使用 switch 或三元運算子來簡化部分邏輯。', '{"1": "S", "2": "S", "3": "S", "4": "B"}', NOW(), NOW(), NOW()),
(2000, 602, 1, 'SUCCESS', '## 導師 Code Review\n\n邏輯判斷非常嚴謹！成功處理了各種輸入情況。代碼整潔度很高，繼續保持！', '{"1": "SSS", "2": "A", "3": "S", "4": "S"}', NOW(), NOW(), NOW());

-- 603: 迴圈迴廊
INSERT INTO gym_challenge_records (user_id, gym_id, gym_challenge_id, status, feedback, ratings, created_at, completed_at, reviewed_at)
VALUES 
(2000, 603, 1, 'SUCCESS', '## 導師的評價\n\n迴圈的使用很標準，避免了無限迴圈的可能。建議在迴圈內減少不必要的計算以優化效能。', '{"1": "A", "2": "S", "3": "S", "4": "S"}', NOW(), NOW(), NOW()),
(2000, 603, 1, 'SUCCESS', '## 導師 Code Review\n\nfor 迴圈與陣列的結合非常流暢。可以嘗試挑戰使用 while 迴圈實作同樣功能，體會兩者的差異。', '{"1": "S", "2": "SSS", "3": "A", "4": "S"}', NOW(), NOW(), NOW());

-- 604: 函式神殿
INSERT INTO gym_challenge_records (user_id, gym_id, gym_challenge_id, status, feedback, ratings, created_at, completed_at, reviewed_at)
VALUES 
(2000, 604, 1, 'SUCCESS', '## 導師的評價\n\n函式封裝得很好，單一職責原則落實得不錯。建議為函式撰寫簡單的 JSDoc，提升代碼可讀性。', '{"1": "S", "2": "S", "3": "A", "4": "S"}', NOW(), NOW(), NOW()),
(2000, 604, 1, 'SUCCESS', '## 導師 Code Review\n\n參數傳遞與回傳值的處理非常精準。可以看到您對作用域 (Scope) 有很好的理解。', '{"1": "S", "2": "S", "3": "S", "4": "SSS"}', NOW(), NOW(), NOW());

-- 605: 陣列峽谷
INSERT INTO gym_challenge_records (user_id, gym_id, gym_challenge_id, status, feedback, ratings, created_at, completed_at, reviewed_at)
VALUES 
(2000, 605, 1, 'SUCCESS', '## 導師的評價\n\n陣列方法的應用 (map, filter) 非常熟練，展現了強大的資料處理能力。建議多注意深淺拷貝的問題。', '{"1": "SSS", "2": "S", "3": "S", "4": "S"}', NOW(), NOW(), NOW()),
(2000, 605, 1, 'SUCCESS', '## 導師 Code Review\n\n對陣列的操作很直覺，索引處理沒有錯誤。繼續練習更複雜的巢狀陣列處理。', '{"1": "S", "2": "A", "3": "S", "4": "S"}', NOW(), NOW(), NOW());

-- 606: 物件之塔
INSERT INTO gym_challenge_records (user_id, gym_id, gym_challenge_id, status, feedback, ratings, created_at, completed_at, reviewed_at)
VALUES 
(2000, 606, 1, 'SUCCESS', '## 導師的評價\n\n物件結構設計得十分合理，屬性與方法的分配很契合主題。建議練習使用解構賦值 (Destructuring) 來簡化存取。', '{"1": "A", "2": "S", "3": "S", "4": "S"}', NOW(), NOW(), NOW()),
(2000, 606, 1, 'SUCCESS', '## 導師 Code Review\n\n完美利用了物件來描述實體特徵。對 JSON 格式的理解也很到位。做得好！', '{"1": "S", "2": "S", "3": "S", "4": "SSS"}', NOW(), NOW(), NOW());
