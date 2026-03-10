-- Remove project management and Git related tables
-- Execute manually in environments that already contain these tables.

DROP TABLE IF EXISTS project_services;
DROP TABLE IF EXISTS project_apis;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS git_users;
