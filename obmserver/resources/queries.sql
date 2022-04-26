-- :name find-localities-by-state-id :? :*
-- :doc load localities by given state id
SELECT * FROM obm_location WHERE state_code = :state_code AND ltype = 'H';

-- :name find-locality-by-id :? :1
-- :doc load locality by given id
SELECT * FROM obm_location WHERE id = :id;

-- :name find-species-by-locality-id :? :*
-- :doc load species types by given locality id
SELECT DISTINCT species_id FROM obm_record WHERE locality_id = :locality_id;

-- :name find-species-by-locality-id-and-month :? :*
-- :doc filter species types for locality by month
SELECT DISTINCT species_id FROM obm_record WHERE locality_id = :locality_id AND month(record_date) = :month;

-- :name find-species-by-ids :? :*
-- :doc query species by given ids
SELECT * FROM obm_species WHERE id in (:v*:ids);

-- :name find-species-by-id :? :1
-- :doc query sepecies information by given id
SELECT * FROM obm_species WHERE id = :id;

-- :name find-records-by-species-and-locality :? :*
-- :doc filter records by species and locality
SELECT * FROM obm_record WHERE locality_id = :locality_id AND species_id = :species_id order by record_date;

-- :name find-localities-records-by-species :? :*
-- :doc find all localities with given species_id
SELECT locality_id, c, l.lname FROM (SELECT locality_id, count(*) AS c FROM obm_record WHERE species_id = :species_id GROUP BY locality_id) r INNER JOIN obm_location l ON r.locality_id = l.id WHERE l.state_code = :state_code AND l.ltype = 'H' ORDER BY c DESC;
