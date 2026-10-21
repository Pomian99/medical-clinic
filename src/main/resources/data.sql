-- Runs on every application start, so every insert must be safe to repeat.
INSERT INTO patients (email, password, id_card_no, first_name, last_name,
                     phone_number, birthday)
VALUES ('anna.nowak@example.com', 'Silne123!', 'ABC123456',
        'Anna', 'Nowak', '500600700', DATE '1990-05-12'),
       ('jan.wisniewski@example.com', 'Silne456!', 'ABC123457',
        'Jan', 'Wiśniewski', '500600701', DATE '1985-11-30'),
       ('ewa.kowalczyk@example.com', 'Silne789!', 'ABC123458',
        'Ewa', 'Kowalczyk', '500600702', DATE '1978-03-21')
    ON CONFLICT (email) DO NOTHING;
