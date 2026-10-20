INSERT INTO education_stage (name, code, description, contact_hours, online_hours, practical_hours, previous_stage_required)
VALUES ('Formador/a', 'AF',
        'Etapa de Formador/a (EAF): políticas y programación de acciones formativas, recursos didácticos, impartición y tutorización, evaluación y prácticas de formador.',
        20, 10, 70, 0),
       ('Formador/a de Scouters', 'FS',
        'Etapa de Formador/a de Scouters (EFS): sistema de formación y cualificación, programación, elementos didácticos, psicopedagogía, evaluación, gestión de la formación y prácticas.',
        35, 15, 150, 0),
       ('Formador/a de Formadores/as', 'FF',
        'Etapa de Formador/a de Formadores/as (EFF): programación, elementos didácticos, tutorización, evaluación, gestión de la formación, investigación y desarrollo de acciones formativas y prácticas.',
        35, 15, 150, 0);

UPDATE education_stage
SET previous_stage_id = (SELECT id FROM (SELECT id FROM education_stage WHERE code = 'AF') s)
WHERE code = 'FS';

UPDATE education_stage
SET previous_stage_id = (SELECT id FROM (SELECT id FROM education_stage WHERE code = 'FS') s)
WHERE code = 'FF';

ALTER TABLE user
    ADD trainer_stage_id INT NULL;

ALTER TABLE user
    ADD CONSTRAINT FK_USER_ON_TRAINER_STAGE FOREIGN KEY (trainer_stage_id) REFERENCES education_stage (id) ON DELETE SET NULL;
