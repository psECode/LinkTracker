import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as api from '../api/client';

export default function LinksPage() {
  const queryClient = useQueryClient();
  const linksQuery = useQuery({ queryKey: ['links'], queryFn: api.getLinks });

  const [url, setUrl] = useState('');
  const [tags, setTags] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const addMutation = useMutation({
    mutationFn: (vars: { url: string; tags: string[] }) => api.addLink(vars.url, vars.tags),
    onSuccess: () => {
      setUrl('');
      setTags('');
      setFormError(null);
      queryClient.invalidateQueries({ queryKey: ['links'] });
    },
    onError: (err) => setFormError(api.apiErrorMessage(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (linkUrl: string) => api.removeLink(linkUrl),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['links'] });
    },
    onError: (err) => setFormError(api.apiErrorMessage(err)),
  });

  const handleAdd = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmedUrl = url.trim();
    if (trimmedUrl === '') {
      setFormError('Введите URL ссылки');
      return;
    }
    const tagList = tags
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
    addMutation.mutate({ url: trimmedUrl, tags: tagList });
  };

  return (
    <div className="page">
      <div className="page-head">
        <h1>Отслеживаемые ссылки</h1>
        {linksQuery.data !== undefined && (
          <span className="muted">{linksQuery.data.size} шт.</span>
        )}
      </div>

      <form className="card form-card" onSubmit={handleAdd}>
        <div className="form-row">
          <input
            className="input"
            type="url"
            placeholder="https://example.com/article"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            aria-label="URL для отслеживания"
            required
          />
          <input
            className="input"
            type="text"
            placeholder="Теги через запятую: новости, it"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            aria-label="Теги через запятую"
          />
          <button type="submit" className="btn btn-primary" disabled={addMutation.isPending}>
            {addMutation.isPending ? 'Добавляем…' : 'Добавить'}
          </button>
        </div>
        {formError !== null && <div className="alert alert-error">{formError}</div>}
      </form>

      {linksQuery.isPending && <div className="card empty">Загрузка…</div>}
      {linksQuery.isError && (
        <div className="alert alert-error">{api.apiErrorMessage(linksQuery.error)}</div>
      )}

      {linksQuery.data !== undefined && linksQuery.data.links.length === 0 && (
        <div className="card empty">Нет отслеживаемых ссылок</div>
      )}

      {linksQuery.data !== undefined && linksQuery.data.links.length > 0 && (
        <ul className="link-list">
          {linksQuery.data.links.map((link) => (
            <li key={link.id} className="card link-card">
              <div className="link-main">
                <a href={link.url} target="_blank" rel="noreferrer" className="link-url">
                  {link.url}
                </a>
                {link.tags.length > 0 && (
                  <div className="chips">
                    {link.tags.map((tag, index) => (
                      <span key={`${tag}-${index}`} className="chip">
                        {tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <button
                type="button"
                className="btn btn-danger"
                disabled={removeMutation.isPending}
                onClick={() => removeMutation.mutate(link.url)}
              >
                Отписаться
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}