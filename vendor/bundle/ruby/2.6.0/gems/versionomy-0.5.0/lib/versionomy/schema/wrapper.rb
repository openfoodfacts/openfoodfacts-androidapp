# -----------------------------------------------------------------------------
#
# Versionomy schema wrapper class
#
# -----------------------------------------------------------------------------
# Copyright 2008 Daniel Azuma
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice,
#   this list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the copyright holder, nor the names of any other
#   contributors to this software, may be used to endorse or promote products
#   derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# -----------------------------------------------------------------------------
;


module Versionomy

  module Schema


    # Creates a schema.
    # Returns an object of type Versionomy::Schema::Wrapper.
    #
    # You may either pass a root field, or provide a block to use to build
    # fields. If you provide a block, you must use the methods in
    # Versionomy::Schema::Builder in the block to create the root field.

    def self.create(field_=nil, opts_={}, &block_)
      if field_ && block_
        raise ::ArgumentError, 'You may provide either a root field or block but not both'
      end
      if block_
        builder_ = Schema::Builder.new
        ::Blockenspiel.invoke(block_, builder_)
        field_ = builder_._get_field
        modules_ = builder_._get_modules
        aliases_ = builder_._get_aliases
      else
        modules_ = opts_[:modules] || []
      end
      Schema::Wrapper.new(field_, modules_, aliases_)
    end


    # Schemas are generally referenced through an object of this class.

    class Wrapper


      # Create a new schema wrapper object given a root field.
      # This is a low-level method. Usually you should call
      # Versionomy::Schema#create instead.

      def initialize(field_, modules_=[], aliases_={})
        @root_field = field_
        @names = @root_field._descendants_by_name
        @modules = modules_
        @aliases = {}
        aliases_.each do |k_,v_|
          k_ = k_.to_sym
          v_ = v_.to_sym
          if @names.include?(v_) && !@names.include?(k_)
            @aliases[k_] = v_
          end
        end
      end


      def inspect   # :nodoc:
        "#<#{self.class}:0x#{object_id.to_s(16)} root=#{@root_field.inspect}>"
      end

      def to_s   # :nodoc:
        inspect
      end


      # Returns true if this schema is equivalent to the other schema.
      # Two schemas are equivalent if their root fields are the same--
      # which means that the entire field tree is the same-- and they
      # include the same value modules.
      # Note that this is different from the definition of <tt>==</tt>.

      def eql?(obj_)
        return false unless obj_.kind_of?(Schema::Wrapper)
        return @root_field == obj_.root_field && @modules == obj_.modules && @aliases == obj_.aliases
      end


      # Returns true if this schema is compatible with the other schema.
      # Two schemas are compatible if their root fields are the same--
      # which means that the entire field tree is the same. They may,
      # however, include different value modules.
      # Note that this is different from the definition of <tt>eql?</tt>.

      def ==(obj_)
        eql?(obj_)
      end


      # If the RHS is a schema, returns true if the schemas are equivalent.
      # If the RHS is a value, returns true if the value uses this schema.

      def ===(obj_)
        if obj_.kind_of?(Value)
          obj_.schema == self
        else
          obj_ == self
        end
      end


      def hash  # :nodoc:
        @hash ||= @root_field.hash ^ @modules.hash
      end


      # Returns the root (most significant) field in this schema.

      def root_field
        @root_field
      end


      # Return the canonical field name given a name, or nil if the name
      # is not recognized.

      def canonical_name(name_)
        name_ = name_.to_sym
        name_ = @aliases[name_] || name_
        @names.include?(name_) ? name_ : nil
      end


      # Return the field with the given name, or nil if the given name
      # is not found in this schema. If include_aliases_ is set to true,
      # this also supports lookup by alias.

      def field_named(name_, include_aliases_=false)
        name_ = name_.to_sym
        name_ = @aliases[name_] || name_ if include_aliases_
        @names[name_]
      end


      # Returns an array of names present in this schema, in no particular
      # order. Does not include aliases.

      def names
        @names.keys
      end


      # Returns an array of modules that should be included in values that
      # use this schema.

      def modules
        @modules.dup
      end


      # Returns a hash of field name aliases.

      def aliases
        @aliases.dup
      end


    end


    # These methods are available in a schema definition block given to
    # Versionomy::Schema#create.

    class Builder

      include ::Blockenspiel::DSL

      def initialize()  # :nodoc:
        @field = nil
        @modules = []
        @aliases = {}
        @defaults = { :integer => {}, :string => {}, :symbol => {} }
      end


      # Create the root field.
      #
      # Recognized options include:
      #
      # <tt>:type</tt>::
      #   Type of field. This should be <tt>:integer</tt>, <tt>:string</tt>,
      #   or <tt>:symbol</tt>. Default is <tt>:integer</tt>.
      # <tt>:default_value</tt>::
      #   Default value for the field if no value is explicitly set. Default
      #   is 0 for an integer field, the empty string for a string field, or
      #   the first symbol added for a symbol field.
      #
      # You may provide an optional block. Within the block, you may call
      # methods of Versionomy::Schema::FieldBuilder to customize this field.
      #
      # Raises Versionomy::Errors::IllegalValueError if the given default
      # value is not legal.
      #
      # Raises Versionomy::Errors::RangeOverlapError if a root field has
      # already been created.

      def field(name_, opts_={}, &block_)
        if @field
          raise Errors::RangeOverlapError, "Root field already defined"
        end
        @field = Schema::Field.new(name_, opts_.merge(:master_builder => self), &block_)
      end


      # Create a field alias.

      def alias_field(alias_name_, field_name_)
        @aliases[alias_name_.to_sym] = field_name_.to_sym
      end


      # Add a module to the schema. All values that use this schema will
      # include this module. This provides a way to add schema-specific
      # capabilities to version numbers.

      def add_module(mod_)
        @modules << mod_
      end


      # Provide a default bump procedure for the given type.
      # The type should be <tt>:integer</tt>, <tt>:string</tt>, or
      # <tt>:symbol</tt>. You must provide a block that takes a field value
      # and returns the "bumped" value. This procedure will be used for
      # all fields of this type, unless explicitly overridden by the field.

      def to_bump_type(type_, &block_)
        @defaults[type_][:bump] = block_
      end


      # Provide a default compare procedure for the given type.
      # The type should be <tt>:integer</tt>, <tt>:string</tt>, or
      # <tt>:symbol</tt>. You must provide a block that takes two values
      # and returns a standard comparison result-- that is, a negative
      # integer if the first value is less, 0 if the values are equal, or a
      # positive integer if the first value is greater. This procedure will
      # be used for all fields of this type, unless explicitly overridden
      # by the field.

      def to_compare_type(type_, &block_)
        @defaults[type_][:compare] = block_
      end


      # Provide a default canonicalization procedure for the given type.
      # The type should be <tt>:integer</tt>, <tt>:string</tt>, or
      # <tt>:symbol</tt>. You must provide a block that takes a field value
      # and returns the canonical value. This procedure will be used for
      # all fields of this type, unless explicitly overridden by the field.

      def to_canonicalize_type(type_, &block_)
        @defaults[type_][:canonicalize] = block_
      end


      # Provide a default value for the given type.
      # The type should be <tt>:integer</tt>, <tt>:string</tt>, or
      # <tt>:symbol</tt>. You must provide a default value that will be
      # used for all fields of this type, unless explicitly overridden by
      # the field.

      def default_value_for_type(type_, value_)
        @defaults[type_][:value] = value_
      end


      def _get_field  # :nodoc:
        @field
      end

      def _get_modules  # :nodoc:
        @modules
      end

      def _get_aliases  # :nodoc:
        @aliases
      end

      def _get_default_setting(type_, setting_)  # :nodoc:
        @defaults[type_][setting_]
      end

    end


  end

end
