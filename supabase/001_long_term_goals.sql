-- Run this in the Supabase SQL editor (Project > SQL Editor > New query) once, after enabling
-- the Google provider under Authentication > Providers. This is the sync pilot table for
-- Goals — the same pattern (id uuid pk, user_id default auth.uid(), *_epoch_millis bigint,
-- RLS scoped to auth.uid()) gets repeated for the rest of the entities in a later pass.

create table if not exists public.long_term_goals (
  id uuid primary key,
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  title text not null,
  description text not null default '',
  start_date date,
  deadline_date date,
  tag text,
  progress_percent int not null default 0,
  is_archived boolean not null default false,
  created_at_epoch_millis bigint not null,
  updated_at_epoch_millis bigint not null,
  deleted_at_epoch_millis bigint
);

create index if not exists long_term_goals_user_updated_idx
  on public.long_term_goals (user_id, updated_at_epoch_millis);

alter table public.long_term_goals enable row level security;

drop policy if exists "individuals manage own goals" on public.long_term_goals;
create policy "individuals manage own goals" on public.long_term_goals
  for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);
