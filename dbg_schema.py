import pg8000.native

con = pg8000.native.Connection(
    user="postgres.zmhbdzfifgbxefugovzt",
    password="quizcit2026",
    host="aws-1-ap-northeast-1.pooler.supabase.com",
    port=6543,
    database="postgres",
)

tables = ["responses", "user_skill_ability", "ability_history", "attempts", "question_skill"]
for t in tables:
    print(f"\n===== {t} =====")
    rows = con.run("""
        SELECT column_name, data_type, is_nullable, column_default, character_maximum_length
        FROM information_schema.columns
        WHERE table_name = :t
        ORDER BY ordinal_position
    """, t=t)
    if not rows:
        print("  (table not found)")
    for r in rows:
        print(f"  {r[0]:24} {r[1]:18} null={r[2]:4} len={r[4]} default={r[3]}")

# constraints
print("\n===== CONSTRAINTS =====")
crows = con.run("""
    SELECT tc.table_name, tc.constraint_type, tc.constraint_name
    FROM information_schema.table_constraints tc
    WHERE tc.table_name = ANY(:tabs)
    ORDER BY tc.table_name, tc.constraint_type
""", tabs=tables)
for r in crows:
    print(f"  {r[0]:24} {r[1]:14} {r[2]}")
con.close()
