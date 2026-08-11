-- Prevent concurrent registration from creating duplicate logical roles.
CREATE UNIQUE INDEX IF NOT EXISTS uk_roles_name ON roles (name);
