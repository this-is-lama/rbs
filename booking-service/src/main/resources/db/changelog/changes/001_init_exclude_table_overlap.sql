-- 1) GiST support for uuid (=)
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 2) add column (nullable first)
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS table_id uuid;

-- 3) backfill from snapshot
UPDATE bookings b
SET table_id = bt.table_id
FROM booking_table bt
WHERE bt.booking_id = b.id
  AND b.table_id IS NULL;

-- 4) fail fast if there are nulls
DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM bookings WHERE table_id IS NULL) THEN
            RAISE EXCEPTION 'Cannot set bookings.table_id NOT NULL: found rows with NULL table_id';
        END IF;
    END $$;

ALTER TABLE bookings
    ALTER COLUMN table_id SET NOT NULL;

-- 5) sanity check for time
ALTER TABLE bookings
    DROP CONSTRAINT IF EXISTS chk_bookings_time_valid;

ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_time_valid
        CHECK (start_at < end_at);

-- 6) exclude overlap by table_id + time range, only for active bookings
ALTER TABLE bookings
    DROP CONSTRAINT IF EXISTS ex_bookings_table_no_overlap;

ALTER TABLE bookings
    ADD CONSTRAINT ex_bookings_table_no_overlap
        EXCLUDE USING gist (
        table_id WITH =,
        tstzrange(start_at, end_at, '[)') WITH &&
        )
        WHERE (status = 'RESERVED');

CREATE INDEX IF NOT EXISTS idx_bookings_table_id ON bookings(table_id);
