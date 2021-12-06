-- :name find-localities-by-state-id :? :*
-- :doc load localities by given state id
SELECT * FROM obm_location WHERE state_code = :state_code;
