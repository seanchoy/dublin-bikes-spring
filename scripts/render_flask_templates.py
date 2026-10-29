"""Render the original Flask/Jinja templates into static HTML pages for the Spring Boot app."""
import re, sys
from pathlib import Path
from jinja2 import Environment, FileSystemLoader

TEMPLATES = Path(sys.argv[1]); OUT = Path(sys.argv[2])

ROUTES = {
    "frontend.home": "/", "frontend.weather_page": "/weather.html",
    "journey.index": "/journey.html", "journey.page": "/journey.html",
    "stations.stations_page": "/stations.html", "stations.stations": "/api/v1/stations",
    "stations.stations_predict": "/api/stations/predict",
    "auth.login": "#", "auth.register": "#", "auth.logout": "#",
}

def url_for(endpoint, **kw):
    if endpoint in ("static", "frontend.static") or endpoint.endswith(".static"):
        return "/" + kw["filename"]
    return ROUTES[endpoint]

env = Environment(loader=FileSystemLoader(str(TEMPLATES)))
env.globals.update(url_for=url_for, session={}, config={}, get_flashed_messages=lambda **k: [], request=None)

for src, dst in [("index.html", "index.html"), ("weather.html", "weather.html"),
                 ("stations.html", "stations.html"), ("Journey.html", "journey.html")]:
    html = env.get_template(src).render()
    # compatibility layer must load before any page script
    html = re.sub(r"<head>", '<head>\n  <script src="/js/api-compat.js"></script>', html, count=1)
    html = re.sub(r"\n\s*\n\s*\n+", "\n\n", html)
    (OUT / dst).write_text(html, encoding="utf-8")
    print("wrote", dst, len(html))
