-- This script adds all of the required data into the database.

-- Need at least one trainer.
insert into trainer values (1, 'Ash Ketchum', '(760)555-5555');

-- junior courses
insert into course values ('Strength 001', 1, 'monday', CURRENT_DATE, to_date('11/2/2025','MM/DD/YYYY'), 1200, 1300, 0, 100);
insert into course values ('Yoga 001', 1, 'tuesday', CURRENT_DATE, to_date('11/2/2025','MM/DD/YYYY'), 1200, 1300, 0, 100);

-- senior courses
insert into course values ('Strength 002', 1, 'wednesday', CURRENT_DATE, to_date('11/2/2025','MM/DD/YYYY'), 1200, 1300, 0, 100);
insert into course values ('Yoga 002', 1, 'thursday', CURRENT_DATE, to_date('11/2/2025','MM/DD/YYYY'), 1200, 1300, 0, 100);

insert into package values ('Junior', 100);
insert into package values ('Senior', 100);
insert into package values ('Strength', 200);

insert into packagecourse values ('Junior', 'Strength 001');
insert into packagecourse values ('Junior', 'Yoga 001');

insert into packagecourse values ('Senior', 'Strength 002');
insert into packagecourse values ('Senior', 'Yoga 002');

insert into packagecourse values ('Strength', 'Strength 001');
insert into packagecourse values ('Strength', 'Strength 002');

insert into levels values ('Gold', 20, 1000);
insert into levels values ('Diamond', 10, 500);
insert into levels values ('Regular', 0, 0);