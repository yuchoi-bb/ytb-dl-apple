const searchForm = document.getElementById('searchForm');
const searchInput = document.getElementById('searchInput');
const statusMsg = document.getElementById('statusMsg');
const resultsGrid = document.getElementById('resultsGrid');
const cardTemplate = document.getElementById('cardTemplate');

function formatDuration(seconds) {
  if (!seconds) return '';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  return `${m}:${String(s).padStart(2, '0')}`;
}

function formatViews(count) {
  if (!count) return '';
  if (count >= 1_000_000) return `${(count / 1_000_000).toFixed(1)}M 뷰`;
  if (count >= 1_000) return `${(count / 1_000).toFixed(0)}K 뷰`;
  return `${count} 뷰`;
}

function showStatus(msg, isError = false) {
  statusMsg.innerHTML = msg;
  statusMsg.className = 'status-msg' + (isError ? ' error' : '');
}

function clearStatus() {
  statusMsg.className = 'status-msg hidden';
  statusMsg.innerHTML = '';
}

function renderCards(results) {
  resultsGrid.innerHTML = '';
  if (!results.length) {
    showStatus('검색 결과가 없습니다.');
    return;
  }
  clearStatus();

  results.forEach((item) => {
    const clone = cardTemplate.content.cloneNode(true);
    const card = clone.querySelector('.card');

    const img = clone.querySelector('.thumb-img');
    img.src = item.thumbnail || '';
    img.alt = item.title;

    const badge = clone.querySelector('.duration-badge');
    const dur = formatDuration(item.duration);
    badge.textContent = dur;
    if (!dur) badge.style.display = 'none';

    clone.querySelector('.card-title').textContent = item.title;
    clone.querySelector('.card-channel').textContent = item.channel;
    clone.querySelector('.card-views').textContent = formatViews(item.viewCount);

    const dlBtn = clone.querySelector('.dl-btn');
    dlBtn.addEventListener('click', () => downloadMp3(item, dlBtn));

    resultsGrid.appendChild(clone);
  });
}

async function search(query) {
  resultsGrid.innerHTML = '';
  showStatus(`<span class="spinner"></span> "${query}" 검색 중...`);

  try {
    const res = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || '검색 실패');
    renderCards(data.results);
  } catch (err) {
    showStatus(`오류: ${err.message}`, true);
  }
}

function downloadMp3(item, btn) {
  const originalHtml = btn.innerHTML;
  btn.classList.add('loading');
  btn.innerHTML = `<span class="dl-icon">&#8595;</span> 변환 중`;

  const url = `/api/download?id=${encodeURIComponent(item.id)}&title=${encodeURIComponent(item.title)}`;
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = '';
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);

  // Reset button after a few seconds
  setTimeout(() => {
    btn.classList.remove('loading');
    btn.innerHTML = originalHtml;
  }, 4000);
}

searchForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const q = searchInput.value.trim();
  if (q) search(q);
});
