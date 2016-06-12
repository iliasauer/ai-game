define([], function () {

    const FIELD_STYLE =
        [
            {
                "selector": "node",
                "style": {
                    "background-color": "white",
                    "border-color": "black",
                    "content": "data(name)",
                    "width": 20,
                    "height": 20,
                    "min-zoomed-font-size": 12,
                    "color": "#fff",
                    "font-size": 16
                }
            },
            {
                "selector": "node:selected,node.start,node.end",
                "style": {
                    "width": 60,
                    "height": 60,
                    "min-zoomed-font-size": 0,
                    "font-size": 48,
                    "border-color": "#000",
                    "border-width": "10px",
                    "text-outline-color": "#000",
                    "text-outline-width": "10px",
                    "z-index": 9999
                }
            },
            {
                "selector": "node.start,node.end",
                "style": {
                    "background-color": "#FC4C4C",
                    "color": "#FC4C4C"
                }
            },
            {
                "selector": "edge",
                "style": {
                    "min-zoomed-font-size": 12,
                    "font-size": 20,
                    "border-color": "#000",
                    "border-width": "4px",
                    "text-outline-color": "#000",
                    "text-outline-width": "4px",
                    "color": "#fff",
                    "content": "data(weight)",
                    "line-color": "green",
                    "width": 20,
                    "curve-style": "haystack",
                    "haystack-radius": 0,
                    "opacity": 0.7
                }
            },
            {
                "selector": "edge[type = 0]",
                "style": {
                    "line-color": "#00FFFF"
                }
            },
            {
                "selector": "edge[type = 1]",
                "style": {
                    "line-color": "#00FF33"
                }
            },
            {
                "selector": "edge[type = 2]",
                "style": {
                    "line-color": "white"
                }
            },
            {
                "selector": "edge[type = 3]",
                "style": {
                    "line-color": "red"
                }
            },
            {
                "selector": "core",
                "style": {
                    "active-bg-color": "#fff",
                    "active-bg-opacity": 0.333
                }
            },
            {
                "selector": "edge.not-path",
                "style": {
                    "opacity": 0.1
                }
            },
            {
                "selector": "node.not-path",
                "style": {
                    "opacity": 0.333
                }
            },
            {
                "selector": "edge.path",
                "style": {
                    "opacity": 0.666
                }
            }
        ];

    return {
        FIELD_STYLE: FIELD_STYLE
    }

});