const ChromaWaves = (() => {
  let canvas, ctx, width, height, animId;
  let time = 0;

  // Golden particle dust motes for atmospheric depth
  const particles = [];
  const PARTICLE_COUNT = 32;

  function initParticles() {
    particles.length = 0;
    for (let i = 0; i < PARTICLE_COUNT; i++) {
      particles.push({
        x: Math.random() * (width || window.innerWidth),
        y: Math.random() * (height || window.innerHeight),
        radius: Math.random() * 1.6 + 0.6,
        alpha: Math.random() * 0.4 + 0.1,
        speedX: (Math.random() - 0.5) * 0.35,
        speedY: -Math.random() * 0.4 - 0.1,
        pulseSpeed: Math.random() * 0.02 + 0.01,
        pulseOffset: Math.random() * Math.PI * 2
      });
    }
  }

  function resize() {
    if (!canvas) return;
    width = window.innerWidth;
    height = window.innerHeight;
    canvas.width = width;
    canvas.height = height;
    if (particles.length === 0) initParticles();
  }

  // Draw smooth undulating wave ribbon
  function drawWaveRibbon(baseY, amplitude, freq, speed, colorStops, verticalThickness) {
    ctx.beginPath();
    ctx.moveTo(0, height);

    // Starting point
    const startY = baseY + Math.sin(time * speed) * amplitude;
    ctx.lineTo(0, startY);

    const step = 40;
    for (let x = 0; x <= width + step; x += step) {
      const y = baseY +
        Math.sin(x * freq + time * speed) * amplitude +
        Math.cos(x * freq * 0.6 - time * speed * 0.7) * (amplitude * 0.45) +
        Math.sin((x + baseY) * 0.001 + time * speed * 0.4) * (amplitude * 0.25);

      ctx.lineTo(x, y);
    }

    ctx.lineTo(width, height);
    ctx.closePath();

    // Create vertical gradient for rich shimmering ribbon
    const grad = ctx.createLinearGradient(0, baseY - amplitude, 0, baseY + verticalThickness);
    colorStops.forEach(stop => grad.addColorStop(stop.pos, stop.color));
    ctx.fillStyle = grad;
    ctx.fill();
  }

  function draw() {
    if (!ctx) return;
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';

    // 1. Base Canvas Background
    if (isDark) {
      // Midnight Obsidian with deep Ceylon Emerald core
      const bgGrad = ctx.createRadialGradient(
        width * 0.5, height * 0.25, 50,
        width * 0.5, height * 0.5, Math.max(width, height) * 0.85
      );
      bgGrad.addColorStop(0, '#0f241a');   // Ceylon deep emerald glow
      bgGrad.addColorStop(0.55, '#091510'); // Dark obsidian transition
      bgGrad.addColorStop(1, '#050a08');   // Pure noir edge
      ctx.fillStyle = bgGrad;
    } else {
      // Alabaster Silk with champagne warmth
      const bgGrad = ctx.createRadialGradient(
        width * 0.5, height * 0.15, 50,
        width * 0.5, height * 0.6, Math.max(width, height) * 0.85
      );
      bgGrad.addColorStop(0, '#fdfbf7');   // Warm alabaster cream
      bgGrad.addColorStop(0.6, '#f7f2e7'); // Champagne silk
      bgGrad.addColorStop(1, '#eee6d5');   // Warm stone rim
      ctx.fillStyle = bgGrad;
    }
    ctx.fillRect(0, 0, width, height);

    // 2. Layered Fluid Waves
    if (isDark) {
      // Dark Mode: Deep Emerald Currents + Champagne Gold & Bronze Silk Ribbons

      // Layer 1: Deep Ceylon Forest Flow (slow deep background wave)
      drawWaveRibbon(height * 0.65, 80, 0.0018, 0.25, [
        { pos: 0, color: 'rgba(18, 48, 35, 0.55)' },
        { pos: 0.5, color: 'rgba(10, 26, 19, 0.35)' },
        { pos: 1, color: 'rgba(5, 12, 9, 0.85)' }
      ], height * 0.5);

      // Layer 2: Spiced Bronze & Amber Current
      drawWaveRibbon(height * 0.52, 95, 0.0022, -0.32, [
        { pos: 0, color: 'rgba(168, 120, 48, 0.22)' },
        { pos: 0.4, color: 'rgba(120, 80, 28, 0.18)' },
        { pos: 1, color: 'rgba(7, 18, 13, 0)' }
      ], height * 0.45);

      // Layer 3: Champagne Gold Ribbon (crisp luminous crest)
      drawWaveRibbon(height * 0.40, 70, 0.0028, 0.38, [
        { pos: 0, color: 'rgba(230, 195, 115, 0.28)' },
        { pos: 0.3, color: 'rgba(212, 175, 55, 0.18)' },
        { pos: 0.8, color: 'rgba(140, 100, 30, 0.06)' },
        { pos: 1, color: 'rgba(0, 0, 0, 0)' }
      ], height * 0.4);

      // Layer 4: Upper Gossamer Gilded Shimmer
      drawWaveRibbon(height * 0.26, 55, 0.0032, -0.22, [
        { pos: 0, color: 'rgba(245, 222, 150, 0.15)' },
        { pos: 0.4, color: 'rgba(180, 140, 50, 0.08)' },
        { pos: 1, color: 'rgba(0, 0, 0, 0)' }
      ], height * 0.35);

    } else {
      // Light Mode: Alabaster Silk + Warm Champagne Gold + Gentle Ceylon Tea Sage

      // Layer 1: Ceylon Tea Sage Base
      drawWaveRibbon(height * 0.68, 70, 0.0018, 0.24, [
        { pos: 0, color: 'rgba(175, 205, 190, 0.32)' },
        { pos: 0.6, color: 'rgba(210, 225, 218, 0.22)' },
        { pos: 1, color: 'rgba(240, 235, 225, 0.6)' }
      ], height * 0.45);

      // Layer 2: Warm Spiced Cinnamon & Terracotta tint
      drawWaveRibbon(height * 0.50, 85, 0.0022, -0.30, [
        { pos: 0, color: 'rgba(200, 145, 95, 0.14)' },
        { pos: 0.5, color: 'rgba(225, 185, 135, 0.10)' },
        { pos: 1, color: 'rgba(250, 245, 235, 0)' }
      ], height * 0.4);

      // Layer 3: Champagne Gold Ribbon
      drawWaveRibbon(height * 0.36, 60, 0.0028, 0.35, [
        { pos: 0, color: 'rgba(212, 175, 55, 0.18)' },
        { pos: 0.4, color: 'rgba(229, 192, 123, 0.12)' },
        { pos: 1, color: 'rgba(255, 255, 255, 0)' }
      ], height * 0.35);
    }

    // 3. Subtle Golden Particle Motes (ambient dust floating gracefully)
    ctx.save();
    for (let p of particles) {
      p.x += p.speedX;
      p.y += p.speedY;

      // Wrap around
      if (p.x < 0) p.x = width;
      if (p.x > width) p.x = 0;
      if (p.y < 0) p.y = height;
      if (p.y > height) p.y = 0;

      const alphaPulse = p.alpha * (0.7 + 0.3 * Math.sin(time * 2 + p.pulseOffset));

      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fillStyle = isDark
        ? `rgba(229, 192, 123, ${alphaPulse})` // Warm gold dust
        : `rgba(180, 140, 50, ${alphaPulse * 0.6})`;
      ctx.fill();
    }
    ctx.restore();

    // 4. Subtle Vignette Overlay for focus & legibility
    const vignette = ctx.createRadialGradient(
      width * 0.5, height * 0.5, Math.min(width, height) * 0.3,
      width * 0.5, height * 0.5, Math.max(width, height) * 0.75
    );
    if (isDark) {
      vignette.addColorStop(0, 'rgba(0, 0, 0, 0)');
      vignette.addColorStop(1, 'rgba(4, 8, 6, 0.65)');
    } else {
      vignette.addColorStop(0, 'rgba(255, 255, 255, 0)');
      vignette.addColorStop(1, 'rgba(210, 200, 185, 0.35)');
    }
    ctx.fillStyle = vignette;
    ctx.fillRect(0, 0, width, height);

    time += 0.014;
    animId = requestAnimationFrame(draw);
  }

  function init() {
    // Exclude from admin back-office pages
    if (window.location.pathname.includes('admin')) return;

    // Check if canvas already exists
    if (document.getElementById('chroma-waves-canvas')) return;

    canvas = document.createElement('canvas');
    canvas.id = 'chroma-waves-canvas';
    canvas.style.cssText = `
      position: fixed;
      top: 0; left: 0;
      width: 100%; height: 100%;
      z-index: -1;
      pointer-events: none;
    `;
    document.body.insertBefore(canvas, document.body.firstChild);
    ctx = canvas.getContext('2d');
    resize();
    window.addEventListener('resize', resize);
    draw();
  }

  function destroy() {
    if (animId) cancelAnimationFrame(animId);
    if (canvas) canvas.remove();
    canvas = ctx = null;
  }

  function onThemeChange() {
    // The next draw frame immediately reads document.documentElement.getAttribute('data-theme')
  }

  return { init, destroy, onThemeChange };
})();

document.addEventListener('DOMContentLoaded', () => ChromaWaves.init());
