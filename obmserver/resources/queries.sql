-- :name find-localities-by-state-id :? :*
-- :doc load localities by given state id
SELECT * FROM obm_location WHERE state_code = :state_code AND ltype = 'H';

-- :name find-locality-by-id :? :1
-- :doc load locality by given id
SELECT * FROM obm_location WHERE id = :id;

-- :name find-species-by-locality-id :? :*
-- :doc load species types by given locality id
SELECT DISTINCT species_id FROM obm_record WHERE locality_id = :locality_id;

-- :name find-species-by-ids :? :*
-- :doc query species by given ids
SELECT * FROM obm_species WHERE id in (:v*:ids);
