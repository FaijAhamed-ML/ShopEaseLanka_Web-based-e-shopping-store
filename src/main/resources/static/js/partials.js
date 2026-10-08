/**
 * ShopEase Lanka - Partial loader
 * Replaces every <div data-partial="path.html"></div> with the HTML fetched from that path.
 * This is how each member's HTML (tabs, modals, review section) lives in its own file.
 * Usage: await Partials.loadAll();   (call before touching the injected elements)
 */
const Partials = {
  async loadAll(root = document) {
    const nodes = Array.from(root.querySelectorAll("[data-partial]"));
    await Promise.all(nodes.map(async (node) => {
      const url = node.getAttribute("data-partial");
      try {
        const res = await fetch(url);
        if (!res.ok) throw new Error("HTTP " + res.status);
        node.outerHTML = await res.text();
      } catch (err) {
        console.warn("Partial not available (is that member's branch merged?):", url, err.message);
        node.remove();
      }
    }));
  }
};
