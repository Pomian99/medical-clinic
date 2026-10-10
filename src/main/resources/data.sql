-- Runs on every application start, so every insert must be safe to repeat.

-- Accounts go first: every patient points to one through user_id.
INSERT INTO users (email, password, role)
VALUES ('anna.nowak@example.com', 'Silne123!', 'PATIENT'),
       ('jan.wisniewski@example.com', 'Silne456!', 'PATIENT'),
       ('ewa.kowalczyk@example.com', 'Silne789!', 'PATIENT')
    ON CONFLICT (email) DO NOTHING;

-- Account ids are generated, so each patient looks its account up by e-mail.
INSERT INTO patients (user_id, id_card_no, first_name, last_name,
                     phone_number, birthday)
VALUES ((SELECT id FROM users WHERE email = 'anna.nowak@example.com'), 'ABC123456',
        'Anna', 'Nowak', '500600700', DATE '1990-05-12'),
       ((SELECT id FROM users WHERE email = 'jan.wisniewski@example.com'), 'ABC123457',
        'Jan', 'Wiśniewski', '500600701', DATE '1985-11-30'),
       ((SELECT id FROM users WHERE email = 'ewa.kowalczyk@example.com'), 'ABC123458',
        'Ewa', 'Kowalczyk', '500600702', DATE '1978-03-21')
    ON CONFLICT (user_id) DO NOTHING;
