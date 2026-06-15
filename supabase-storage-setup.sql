-- Ejecutar en Supabase SQL Editor.
-- Crea el bucket publico usado por la app Android para imagenes.

insert into storage.buckets (id, name, public)
values ('app-images', 'app-images', true)
on conflict (id) do update set public = true;

drop policy if exists "Public read app images" on storage.objects;
create policy "Public read app images"
on storage.objects
for select
using (bucket_id = 'app-images');

drop policy if exists "Public upload app images demo" on storage.objects;
create policy "Public upload app images demo"
on storage.objects
for insert
with check (bucket_id = 'app-images');

drop policy if exists "Public update app images demo" on storage.objects;
create policy "Public update app images demo"
on storage.objects
for update
using (bucket_id = 'app-images')
with check (bucket_id = 'app-images');
