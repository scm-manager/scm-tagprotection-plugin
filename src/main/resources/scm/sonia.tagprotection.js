/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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