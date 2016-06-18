"""
WSGI config for eventual project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/1.9/howto/deployment/wsgi/
"""

import os
import sys
import site

# Add the site-packages of virtualenv to work with
site.addsitedir('/home/arpan/Envs/eventualvenv/lib/python2.7/site-packages')

# Add the app's directory to the PYTHONPATH
sys.path.append('/home/arpan/eventual')
sys.path.append('/home/arpan/eventual/eventual')

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'eventual.settings')

# Activating virtual env
activate_env=os.path.expanduser("/home/arpan/Envs/eventualvenv/bin/activate_this.py")
execfile(activate_env, dict(__file__=activate_env))

from django.core.wsgi import get_wsgi_application
application = get_wsgi_application()