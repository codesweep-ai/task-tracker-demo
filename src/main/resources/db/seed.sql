-- Seed people (ids 1..4 via IDENTITY).
INSERT INTO person (username, full_name, password, role) VALUES
  ('admin', 'Ada Admin',      'admin', 'ADMIN'),
  ('mgr',   'Morgan Manager', 'mgr',   'MANAGER'),
  ('dev',   'Devon Member',   'dev',   'MEMBER'),
  ('view',  'Vic Viewer',     'view',  'VIEWER');

-- Projects (ids 1..3). owner_id references person.
INSERT INTO project (name, description, state, owner_id) VALUES
  ('Apollo',        'Customer portal revamp',   'ACTIVE',   2),
  ('Borealis',      'Internal billing migration','ACTIVE',  1),
  ('Legacy Archive','Decommissioned in 2024',   'ARCHIVED', 1);

-- Tasks spanning the full status lifecycle. updated_at is staggered a few hours
-- apart so the default "recently updated" sort has an obvious, deterministic
-- order (newest first) before any live mutation bumps a row to the top.
INSERT INTO task (title, description, status, priority, assignee_id, project_id, due_date, updated_at) VALUES
  ('Design login page', 'Mockups + a11y pass',        'OPEN',             'HIGH',   3, 1, DATE '2026-06-15', TIMESTAMP '2026-07-15 09:00:00'),
  ('Wire up SSO',       'OIDC against corp IdP',       'IN_PROGRESS',      'HIGH',   3, 1, DATE '2026-06-20', TIMESTAMP '2026-07-15 12:00:00'),
  ('Migrate invoices',  'Port legacy invoice rows',    'PENDING_APPROVAL', 'MEDIUM', 3, 2, DATE '2026-06-10', TIMESTAMP '2026-07-15 15:00:00'),
  ('Archive old data',  'Cold-storage export',         'DONE',             'LOW',    1, 2, NULL,              TIMESTAMP '2026-07-15 18:00:00');
