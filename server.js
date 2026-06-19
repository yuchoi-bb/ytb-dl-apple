const express = require('express');
const { spawn } = require('child_process');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.static(path.join(__dirname, 'public')));

// YouTube search using yt-dlp
app.get('/api/search', (req, res) => {
  const query = req.query.q;
  if (!query) return res.status(400).json({ error: 'Query required' });

  const args = [
    '--flat-playlist',
    '--dump-json',
    '--no-warnings',
    '--no-check-certificate',
    `ytsearch10:${query}`
  ];

  const proc = spawn('yt-dlp', args);
  let output = '';
  let errOutput = '';

  proc.stdout.on('data', (data) => { output += data.toString(); });
  proc.stderr.on('data', (data) => { errOutput += data.toString(); });

  proc.on('close', (code) => {
    if (code !== 0 && !output) {
      return res.status(500).json({ error: 'Search failed', detail: errOutput });
    }

    const results = output
      .trim()
      .split('\n')
      .filter(Boolean)
      .map((line) => {
        try {
          const d = JSON.parse(line);
          return {
            id: d.id,
            title: d.title || 'Unknown',
            channel: d.channel || d.uploader || 'Unknown',
            duration: d.duration,
            thumbnail: d.thumbnails
              ? d.thumbnails[d.thumbnails.length - 1]?.url
              : `https://img.youtube.com/vi/${d.id}/mqdefault.jpg`,
            viewCount: d.view_count,
            url: d.url || `https://www.youtube.com/watch?v=${d.id}`
          };
        } catch {
          return null;
        }
      })
      .filter(Boolean);

    res.json({ results });
  });
});

// Download as MP3 — streams directly to browser
app.get('/api/download', (req, res) => {
  const videoId = req.query.id;
  const title = req.query.title || 'audio';
  if (!videoId) return res.status(400).json({ error: 'Video ID required' });

  const safeTitle = title.replace(/[^\w\s-]/g, '').trim().replace(/\s+/g, '_');
  const filename = `${safeTitle}.mp3`;

  res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
  res.setHeader('Content-Type', 'audio/mpeg');

  const url = `https://www.youtube.com/watch?v=${videoId}`;
  const args = [
    '-x',
    '--audio-format', 'mp3',
    '--audio-quality', '0',
    '-o', '-',
    '--no-playlist',
    '--no-warnings',
    '--no-check-certificate',
    url
  ];

  const proc = spawn('yt-dlp', args);

  proc.stdout.pipe(res);

  proc.stderr.on('data', (data) => {
    console.error('[yt-dlp]', data.toString());
  });

  proc.on('error', (err) => {
    console.error('Process error:', err);
    if (!res.headersSent) {
      res.status(500).json({ error: 'Download failed' });
    }
  });

  req.on('close', () => {
    proc.kill();
  });
});

app.listen(PORT, () => {
  console.log(`ytudl-web running at http://localhost:${PORT}`);
});
