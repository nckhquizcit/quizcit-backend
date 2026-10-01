create table ability_history( 
    delta float(53) not null, 
    theta float(53) not null, 
    created_at timestamp(6) not null, 
    attempt_id varchar(50), 
    history_id varchar(50) not null, 
    skill_id varchar(50) not null, 
    user_id varchar(50) not null, 
    primary key( 
    history_id));
create table attempts( 
    overall_theta float(53), 
    raw_score float(53), 
    total_question integer, 
    created_at timestamp(6) with time zone, 
    attempt_id varchar(50) not null, 
    quiz_id varchar(50) not null, 
    user_id varchar(50) not null, 
    primary key( 
    attempt_id));
create table chapters( 
    created_at timestamp(6) with time zone, 
    chapter_id varchar(50) not null, 
    subject_id varchar(50) not null, 
    name varchar(200) not null, 
    primary key( 
    chapter_id));
create table options( 
    is_correct boolean, 
    option_id varchar(50) not null, 
    content TEXT, 
    question_id varchar(255) not null, 
    primary key( 
    option_id));
create table permissions( 
    permission_id varchar(50) not null, 
    name varchar(100) not null, 
    primary key( 
    permission_id));
create table question_skills( 
    is_primary boolean, 
    weight float(53) not null, 
    question_id varchar(50) not null, 
    skill_id varchar(50) not null, 
    primary key( 
    question_id, 
    skill_id));
create table questions( 
    difficulty_b float(53), 
    created_at timestamp(6) with time zone, 
    chapter_id varchar(50), 
    question_id varchar(50) not null, 
    subject_id varchar(50), 
    content TEXT, 
    explanation TEXT, 
    primary key( 
    question_id));
create table quiz_questions( 
    index_order integer, 
    question_id varchar(50) not null, 
    quiz_id varchar(50) not null, 
    primary key( 
    question_id, 
    quiz_id));
create table quizzes( 
    duration integer not null, 
    total_question integer, 
    chapter_id varchar(50) not null, 
    quiz_id varchar(50) not null, 
    subject_id varchar(50) not null, 
    name varchar(255) not null, 
    type varchar(255) not null check( 
    type in( 
    'CHAPTER','FINAL','ADAPTIVE')), 
    primary key( 
    quiz_id));
create table responses( 
    score boolean, 
    attempt_id varchar(50) not null, 
    option_id varchar(50), 
    question_id varchar(50) not null, 
    response_id varchar(50) not null, 
    primary key( 
    response_id));
create table role_permissions( 
    permission_id varchar(50) not null, 
    role_id varchar(50) not null, 
    primary key( 
    permission_id, 
    role_id));
create table roles( 
    name varchar(50) not null check( 
    name in( 
    'USER','ADMINISTRATOR')), 
    role_id varchar(50) not null, 
    primary key( 
    role_id));
create table skills( 
    created_at timestamp(6) with time zone, 
    skill_id varchar(50) not null, 
    name varchar(200) not null, 
    description TEXT, 
    primary key( 
    skill_id));
create table subjects( 
    created_at timestamp(6) with time zone, 
    course_code varchar(20) not null, 
    subject_id varchar(50) not null, 
    name varchar(200) not null, 
    description TEXT, 
    primary key( 
    subject_id));
create table theories( 
    created_at timestamp(6) with time zone, 
    theory_id varchar(50) not null, 
    chapter_id varchar(255) not null, 
    content TEXT not null, 
    primary key( 
    theory_id));
create table user_achievement( 
    condition_value integer, 
    is_achieved boolean, 
    achieved_at timestamp(6) with time zone, 
    achieve_id varchar(50) not null, 
    condition_type varchar(50), 
    user_id varchar(50) not null, 
    badge varchar(100), 
    description TEXT, 
    primary key( 
    achieve_id, 
    user_id));
create table user_roles( 
    role_id varchar(50) not null, 
    user_id varchar(50) not null, 
    primary key( 
    role_id, 
    user_id));
create table user_skill_ability( 
    theta float(53), 
    updated_at timestamp(6) with time zone, 
    id varchar(50) not null, 
    skill_id varchar(50), 
    user_id varchar(50), 
    primary key( 
    id), 
    unique( 
    user_id, 
    skill_id));
create table users( 
    created_at timestamp(6) with time zone, 
    status varchar(20) check( 
    status in( 
    'UNVERIFIED','VERIFIED','BANNED')), 
    user_id varchar(50) not null, 
    username varchar(50) not null unique, 
    display_name varchar(100), 
    email varchar(100) not null unique, 
    password varchar(255) not null, 
    primary key( 
    user_id));
