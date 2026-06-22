-- app-images is public for direct file reads. Listing remains disabled.
update storage.buckets
set public = true,
    file_size_limit = 8388608,
    allowed_mime_types = array['image/jpeg', 'image/png', 'image/webp']
where id = 'app-images';

do $$
declare policy_record record;
begin
  for policy_record in
    select policyname
    from pg_policies
    where schemaname = 'storage'
      and tablename = 'objects'
      and cmd = 'SELECT'
      and coalesce(qual, '') like '%app-images%'
  loop
    execute format('drop policy if exists %I on storage.objects', policy_record.policyname);
  end loop;
end $$;
