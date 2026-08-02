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
        var match = document.body.innerText.match(doiRegex);
        if (match) return match[1];
        
        return null;
    })();
    """
}
