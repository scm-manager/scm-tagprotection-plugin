/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

registerGeneralConfigPanel({

    xtype : 'configForm',
    title : 'Tag Protection',
    items : [{
        xtype : 'textfield',
        fieldLabel : 'Protection Pattern',
        name : 'protection-pattern',
        helpText: 'A wildcard pattern to describe the tags that are protected from removal.\n\
               Use * or ? as wildcards.',
        allowBlank : true
    },{
        xtype : 'checkbox',
        fieldLabel : 'Reduce Owner Privilege',
        name : 'reduce-owner-privilege',
        helpText: 'Owners are allowed to remove any tag in their repositories by default. However, when checked \n\
               they may only remove tags that are not protected, i. e. like regular users.',
        inputValue: 'true'
    }],

    onSubmit: function(values){
        this.el.mask('Submit ...');
        Ext.Ajax.request({
            url: restUrl + 'config/tagprotection.json',
            method: 'POST',
            jsonData: values,
            scope: this,
            disableCaching: true,
            success: function(response){
                this.el.unmask();
            },
            failure: function(){
                this.el.unmask();
            }
        });
    },

    onLoad: function(el){
        var tid = setTimeout( function(){ el.mask('Loading ...'); }, 100);
        Ext.Ajax.request({
            url: restUrl + 'config/tagprotection.json',
            method: 'GET',
            scope: this,
            disableCaching: true,
            success: function(response){
                var obj = Ext.decode(response.responseText);
                this.load(obj);
                clearTimeout(tid);
                el.unmask();
            },
            failure: function(){
                el.unmask();
                clearTimeout(tid);
                alert('failure');
            }
        });
    }
});