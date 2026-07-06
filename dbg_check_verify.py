import pg8000.native

con = pg8000.native.Connection(
    user="postgres.zmhbdzfifgbxefugovzt",
    password="quizcit2026",
    host="aws-1-ap-northeast-1.pooler.supabase.com",
    port=6543,
    database="postgres",
)

print("===== ALL TABLES in public schema =====")
rows = con.run("""
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public'
    ORDER BY table_name
""")
for r in rows:
    print(" ", r[0])

print("\n===== Tables matching verify/otp/token/code/email =====")
rows = con.run("""
    SELECT table_schema, table_name
    FROM information_schema.tables
    WHERE table_name ILIKE ANY (ARRAY['%verif%','%otp%','%token%','%_code%','%confirm%','%email%'])
    ORDER BY table_schema, table_name
""")
if not rows:
    print("  (none found)")
for r in rows:
    print(f"  {r[0]}.{r[1]}")

print("\n===== users table columns =====")
rows = con.run("""
    SELECT column_name, data_type, is_nullable, column_default, character_maximum_length
    FROM information_schema.columns
    WHERE table_name = 'users' AND table_schema = 'public'
    ORDER BY ordinal_position
""")
for r in rows:
    print(f"  {r[0]:20} {r[1]:18} null={r[2]:4} len={r[4]} default={r[3]}")

print("\n===== user_status_enum values =====")
rows = con.run("""
    SELECT e.enumlabel
    FROM pg_type t
    JOIN pg_enum e ON e.enumtypid = t.oid
    WHERE t.typname = 'user_status_enum'
    ORDER BY e.enumsortorder
""")
for r in rows:
    print(" ", r[0])

con.close()
