/* 
 *  Copyright (c) 2009, Amostudio,inc
 *  All rights reserved.
 *  Code licensed under the BSD License:
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Amostudio,inc  nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY Amostudio,inc ''AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL Amostudio,inc  BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 *
 * @module NewYUI3Widget
 */

// vars. constants  shortcuts

/**
 *
 *
 * @class NewYUI3Widget
 * @extends Widget
 * @param config {Object} Configuration object
 * @constructor
 */
function NewYUI3Widget() {
    NewYUI3Widget.superclass.constructor.apply(this,arguments);
}

Y.mix(NewYUI3Widget, {

    /**
     * The identity of the widget.
     *
     * @property NewYUI3Widget.NAME
     * @type String
     * @static
     */
    NAME : "newyui3widget",

    /**
     * Static Object hash used to capture existing markup for progressive
     * enhancement.  Keys correspond to config attribute names and values
     * are selectors used to inspect the contentBox for an existing node
     * structure.
     *
     * @property NewYUI3Widget.HTML_PARSER
     * @type Object
     * @protected
     * @static
     */
    HTML_PARSER : {},

    /**
     * Static property used to define the default attribute configuration of
     * the Widget.
     *
     * @property NewYUI3Widget.ATTRS
     * @type Object
     * @protected
     * @static
     */
    ATTRS : { 
        
        /**
         * This attribute is
         * defined by the base Widget class but has an empty value
         *
         * @property NewYUI3Widget.ATTRS
         * @type Object
         * @protected
         * @static
         */
        strings: {
            value: {
                //yourkey:your String value
            }
        }

}
});

Y.extend(NewYUI3Widget, Y.Widget, {


   // config params:
   //  _disabled : false,

    /**
     *
     *
     * @method initializer
     * @protected
     */
    initializer : function () {

        //Signals
        //this.publish(SOMETHING);

    },

    /**
     * Create the DOM structure for the newyui3widget.
     *
     * @method renderUI
     * @protected
     */
    renderUI : function () {

    },


    /**
     *
     * @method bindUI
     * @protected
     */
    bindUI : function () {

    },
    /**
     * Synchronizes the DOM state with the attribute settings
     *
     * @method syncUI
     */
    syncUI : function () {
    },

    /**
    * Destructor lifecycle implementation for the newyui3widget class.
    *
    * @method destructor
    * @protected
    */
    destructor: function() { }

});

Y.NewYUI3Widget = NewYUI3Widget;

