# -*- coding: utf-8 -*-
# Generated by Django 1.9.5 on 2016-07-17 08:05
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('website', '0004_auto_20160717_0635'),
    ]

    operations = [
        migrations.AlterField(
            model_name='eventdata',
            name='ukey',
            field=models.CharField(default=b'qhkylm', max_length=6, primary_key=True, serialize=False),
        ),
    ]