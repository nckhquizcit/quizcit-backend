import pg8000.native, uuid, datetime

con = pg8000.native.Connection(
    user="postgres.zmhbdzfifgbxefugovzt", password="quizcit2026",
    host="aws-1-ap-northeast-1.pooler.supabase.com", port=6543, database="postgres",
)

USER="40016d2b-5f8c-40aa-9c0a-8e812ad86da8"
ATTEMPT="5a1e1b1b-bf16-4512-b893-70fb58e4da30"
Q1="f211ae1e-c0ea-4177-996a-aec1c4de33c0"
OPT1="6b5ae0d7-1e24-41fe-ab4a-d58e36877449"
SKILL="97c48c77-ec0c-409c-83cd-3a8d83666819"
now = datetime.datetime.now(datetime.timezone.utc)

def trial(label, fn):
    try:
        fn(); con.run("ROLLBACK")  # never commit; just see if it errors
        print(f"OK    {label}")
    except Exception as e:
        con.run("ROLLBACK")
        print(f"FAIL  {label}: {type(e).__name__}: {str(e)[:300]}")

trial("insert responses", lambda: con.run(
    "INSERT INTO responses(response_id,attempt_id,question_id,option_id,score) VALUES(:a,:b,:c,:d,:e)",
    a=str(uuid.uuid4()), b=ATTEMPT, c=Q1, d=OPT1, e=True))

trial("insert user_skill_ability", lambda: con.run(
    "INSERT INTO user_skill_ability(id,user_id,skill_id,theta,updated_at) VALUES(:a,:b,:c,:d,:e)",
    a=str(uuid.uuid4()), b=USER, c=SKILL, d=0.5, e=now))

trial("insert ability_history", lambda: con.run(
    "INSERT INTO ability_history(history_id,attempt_id,user_id,skill_id,theta,delta,created_at) VALUES(:a,:b,:c,:d,:e,:f,:g)",
    a=str(uuid.uuid4()), b=ATTEMPT, c=USER, d=SKILL, e=0.5, f=0.5, g=now))

trial("update attempts", lambda: con.run(
    "UPDATE attempts SET raw_score=:r, overall_theta=:o WHERE attempt_id=:a",
    r=1.0, o=0.5, a=ATTEMPT))

# Does our test user actually exist in users?
u = con.run("SELECT user_id FROM users WHERE user_id=:u", u=USER)
print("user exists:", bool(u))
con.close()
