import pg8000.native

con = pg8000.native.Connection(
    user="postgres.zmhbdzfifgbxefugovzt", password="quizcit2026",
    host="aws-1-ap-northeast-1.pooler.supabase.com", port=6543, database="postgres",
)
ATTEMPT="5a1e1b1b-bf16-4512-b893-70fb58e4da30"
QUIZ="0f97d02e-0f04-4ac2-89ae-89dd9c90849c"

print("responses for attempt:", con.run("SELECT count(*) FROM responses WHERE attempt_id=:a", a=ATTEMPT))
print("ability_history for attempt:", con.run("SELECT count(*) FROM ability_history WHERE attempt_id=:a", a=ATTEMPT))
print("\nquiz_questions for quiz:")
for r in con.run("SELECT question_id, index_order FROM quiz_questions WHERE quiz_id=:q ORDER BY index_order", q=QUIZ):
    print("  ", r)

print("\nquestion_skills (weights/skill) per question:")
for r in con.run("""
    SELECT qs.question_id, qs.skill_id, qs.weight, qs.is_primary, s.name
    FROM question_skills qs LEFT JOIN skills s ON s.skill_id=qs.skill_id
    WHERE qs.question_id IN (SELECT question_id FROM quiz_questions WHERE quiz_id=:q)
""", q=QUIZ):
    print("  ", r)

print("\noptions corrected flags:")
for r in con.run("""
    SELECT option_id, question_id, corrected FROM options
    WHERE question_id IN (SELECT question_id FROM quiz_questions WHERE quiz_id=:q)
""", q=QUIZ):
    print("  ", r)

# Does ANY question in this quiz have a correct option at all?
con.close()
