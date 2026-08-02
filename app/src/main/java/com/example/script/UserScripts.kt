package com.example.script

object UserScripts {

    const val OVERLAY_BLOCKER_JS = """
    (function() {
        if (window.__eduOverlayBlockerInjected) return;
        window.__eduOverlayBlockerInjected = true;

        function removePaywallOverlays() {
            var selectors = [
                '[class*="paywall"]', '[id*="paywall"]',
                '[class*="modal-backdrop"]', '[class*="modal-overlay"]',
                '[class*="cookie-banner"]', '[id*="cookie-banner"]',
                '[class*="subscribe-popup"]', '[id*="subscribe-popup"]',
                '[class*="gate-overlay"]', '[class*="blur-overlay"]',
                'div[style*="position: fixed"][style*="z-index"]',
                'div[style*="position: absolute"][style*="z-index: 9999"]',
                'dialog[open]'
            ];

            selectors.forEach(function(sel) {
                try {
                    var els = document.querySelectorAll(sel);
                    els.forEach(function(el) {
                        // Avoid removing main content containers by mistake
                        if (el.tagName !== 'BODY' && el.tagName !== 'MAIN' && el.offsetWidth < window.innerWidth * 0.98) {
                            el.style.display = 'none';
                        }
                    });
                } catch(e) {}
            });

            // Unlock page scrolling
            document.body.style.overflow = 'auto';
            document.body.style.position = 'static';
            document.documentElement.style.overflow = 'auto';
            
            // Remove blur filters on body/main elements
            var blurred = document.querySelectorAll('[style*="filter: blur"], [class*="blur"]');
            blurred.forEach(function(b) {
                b.style.filter = 'none';
                b.style.webkitFilter = 'none';
            });
        }

        // Run immediately and every 2 seconds for dynamic modals
        removePaywallOverlays();
        setInterval(removePaywallOverlays, 2000);
    })();
    """

    const val EXTRACT_DOI_JS = """
    (function() {
        var doiRegex = /\b(10\.\d{4,9}\/[-._;()/:A-Z0-9]+)\b/i;
        
        // 1. Check meta tags
        var metaTags = ['citation_doi', 'dc.identifier', 'dc.identifier.doi', 'doi', 'bepress_citation_doi'];
        for (var i = 0; i < metaTags.length; i++) {
            var el = document.querySelector('meta[name="' + metaTags[i] + '"], meta[property="' + metaTags[i] + '"]');
            if (el && el.content) {
                var m = el.content.match(doiRegex);
                if (m) return m[1];
            }
        }
        
        // 2. Check full document text
        var match = document.body ? document.body.innerText.match(doiRegex) : null;
        if (match) return match[1];
        
        return null;
    })();
    """

    const val EXTRACT_PAGE_TEXT_JS = """
    (function() {
        try {
            var articleEl = document.querySelector('article') || document.querySelector('main') || document.body;
            if (!articleEl) return "";
            var clone = articleEl.cloneNode(true);
            var removeSelectors = ['script', 'style', 'nav', 'header', 'footer', 'iframe', 'aside', '.ads', '.comments'];
            removeSelectors.forEach(function(s) {
                var els = clone.querySelectorAll(s);
                els.forEach(function(e) { e.remove(); });
            });
            return clone.innerText.replace(/\s+/g, ' ').trim();
        } catch(e) {
            return document.body ? document.body.innerText : "";
        }
    })();
    """

    const val EXTRACT_METADATA_JS = """
    (function() {
        function getMeta(names) {
            for (var i = 0; i < names.length; i++) {
                var el = document.querySelector('meta[name="' + names[i] + '"], meta[property="' + names[i] + '"]');
                if (el && el.content) return el.content.trim();
            }
            return "";
        }

        function getMetaList(names) {
            var list = [];
            names.forEach(function(n) {
                var els = document.querySelectorAll('meta[name="' + n + '"], meta[property="' + n + '"]');
                els.forEach(function(el) {
                    if (el && el.content && list.indexOf(el.content.trim()) === -1) {
                        list.push(el.content.trim());
                    }
                });
            });
            return list;
        }

        var title = getMeta(['citation_title', 'dc.title', 'og:title']) || document.title || "";
        var authors = getMetaList(['citation_author', 'dc.creator', 'author']);
        var journal = getMeta(['citation_journal_title', 'citation_publisher', 'dc.publisher', 'og:site_name']);
        var date = getMeta(['citation_publication_date', 'citation_date', 'dc.date']);
        var doi = getMeta(['citation_doi', 'dc.identifier', 'dc.identifier.doi']);

        var year = "";
        if (date) {
            var m = date.match(/\b(19|20)\d{2}\b/);
            if (m) year = m[0];
        }

        return JSON.stringify({
            title: title,
            authors: authors,
            journal: journal,
            year: year,
            doi: doi
        });
    })();
    """

}
