# -----------------------------------------------------------------------------
#
# Versionomy schema field class
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


require 'set'


module Versionomy

  module Schema


    # Objects of this class represent fields in a schema.

    class Field


      # Create a field with the given name.
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
      # methods of Versionomy::Schema::FieldBuilder to further customize the
      # field, or add child fields.
      #
      # Raises Versionomy::Errors::IllegalValueError if the given default
      # value is not legal.

      def initialize(name_, opts_={}, &block_)
        @name = name_.to_sym
        @type = opts_[:type] || :integer
        if @type == :symbol
          @symbol_info = ::Hash.new
          @symbol_order = ::Array.new
        else
          @symbol_info = nil
          @symbol_order = nil
        end
        @default_value = opts_[:default_value]
        @bump_proc = nil
        @compare_proc = nil
        @canonicalize_proc = nil
        master_builder_ = opts_[:master_builder]
        if master_builder_
          @bump_proc = master_builder_._get_default_setting(@type, :bump)
          @compare_proc = master_builder_._get_default_setting(@type, :compare)
          @canonicalize_proc = master_builder_._get_default_setting(@type, :canonicalize)
          @default_value ||= master_builder_._get_default_setting(@type, :value)
        end
        @ranges = nil
        @default_child = nil
        @children = []
        ::Blockenspiel.invoke(block_, Schema::FieldBuilder.new(self, master_builder_)) if block_
        @default_value = canonicalize_value(@default_value)
      end


      def _set_default_value(value_)  # :nodoc:
        @default_value = value_
      end

      def _add_symbol(symbol_, opts_={})  # :nodoc:
        if @type != :symbol
          raise Errors::TypeMismatchError
        end
        if @symbol_info.has_key?(symbol_)
          raise Errors::SymbolRedefinedError
        end
        @symbol_info[symbol_] = [@symbol_order.size, opts_[:bump]]
        @symbol_order << symbol_
        if @default_value.nil?
          @default_value = symbol_
        end
      end

      def _set_bump_proc(block_)  # :nodoc:
        @bump_proc = block_
      end

      def _set_canonicalize_proc(block_)  # :nodoc:
        @canonicalize_proc = block_
      end

      def _set_compare_proc(block_)  # :nodoc:
        @compare_proc = block_
      end


      def inspect   # :nodoc:
        "#<#{self.class}:0x#{object_id.to_s(16)} name=#{@name}>"
      end

      def to_s   # :nodoc:
        inspect
      end


      # The name of the field.

      def name
        @name
      end


      # The type of the field.
      # Possible values are <tt>:integer</tt>, <tt>:string</tt>, or
      # <tt>:symbol</tt>.

      def type
        @type
      end


      # The default value of the field

      def default_value
        @default_value
      end


      # Returns a list of possible values for this field, if the type is
      # <tt>:symbol</tt>. Returns nil for any other type

      def possible_values
        @symbol_order ? @symbol_order.dup : nil
      end


      # Given a value, bump it to the "next" value.
      # Utilizes a bump procedure if given;
      # otherwise uses default behavior depending on the type.

      def bump_value(value_)
        if @bump_proc
          nvalue_ = @bump_proc.call(value_)
          nvalue_ || value_
        elsif @type == :integer || @type == :string
          value_.next
        else
          info_ = @symbol_info[value_]
          info_ ? info_[1] || value_ : nil
        end
      end


      # Perform a standard comparison on two values.
      # Returns an integer that may be positive, negative, or 0.
      # Utilizes a comparison procedure if given;
      # otherwise uses default behavior depending on the type.

      def compare_values(val1_, val2_)
        if @compare_proc
          @compare_proc.call(val1_, val2_)
        elsif @type == :integer || @type == :string
          val1_ <=> val2_
        else
          info1_ = @symbol_info[val1_]
          info2_ = @symbol_info[val2_]
          info1_ && info2_ ? info1_[0] <=> info2_[0] : nil
        end
      end


      # Given a value, return a "canonical" value for this field.
      # Utilizes a canonicalization procedure if given;
      # otherwise uses default behavior depending on the type.
      #
      # Raises Versionomy::Errors::IllegalValueError if the given value is
      # not legal.

      def canonicalize_value(value_)
        orig_value_ = value_
        if @canonicalize_proc
          value_ = @canonicalize_proc.call(value_)
        else
          case @type
          when :integer
            value_ = value_.to_i rescue nil
          when :string
            value_ = value_.to_s rescue nil
          when :symbol
            value_ = value_.to_sym rescue nil
          end
        end
        if value_.nil? || (@type == :symbol && !@symbol_info.has_key?(value_))
          raise Errors::IllegalValueError, "#{@name} does not allow the value #{orig_value_.inspect}"
        end
        value_
      end


      # Returns the child field associated with the given value.
      # Returns nil if this field has no child for the given value.

      def child(value_)  # :nodoc:
        if @ranges
          @ranges.each do |r_|
            if !r_[0].nil?
              cmp_ = compare_values(r_[0], value_)
              next if cmp_.nil? || cmp_ > 0
            end
            if !r_[1].nil?
              cmp_ = compare_values(r_[1], value_)
              next if cmp_.nil? || cmp_ < 0
            end
            return r_[2]
          end
        end
        @default_child
      end


      # Adds the given child field for the given range.
      #
      # If you provide a range of nil, adds the given child field as the
      # default child for values that do not fall into any other
      # explicitly specified range.
      #
      # Otherwise, the ranges parameter must be an array of "range" objects.
      # Each of these range objects must be either a single String, Symbol,
      # or Integer to specify a single value; or a two-element array or a
      # Range object (only inclusive ends are supported) to specify a range
      # of values.
      #
      # Raises Versionomy::Errors::RangeOverlapError if the specified
      # range overlaps another previously specified range, or if more than
      # one default child has been set.
      #
      # Raises Versionomy::Errors::RangeSpecificationError if the range
      # is incorrectly specified.
      #
      # Raises Versionomy::Errors::CircularDescendantError if adding this
      # child will result in a circular reference.

      def add_child(child_, ranges_=nil)
        if child_._descendant_fields.include?(self)
          raise Errors::CircularDescendantError
        end
        @children << child_
        if ranges_.nil?
          if @default_child
            raise Errors::RangeOverlapError("Cannot have more than one default child")
          end
          @default_child = child_
          return
        end
        ranges_ = [ranges_] unless ranges_.is_a?(Array)
        ranges_.each do |range_|
          case range_
          when ::Range
            if range_.exclude_end?
              raise Errors::RangeSpecificationError("Ranges must be inclusive")
            end
            normalized_range_ = [range_.first, range_.last]
          when ::Array
            if range_.size != 2
              raise Errors::RangeSpecificationError("Range array should have two elements")
            end
            normalized_range_ = range_.dup
          when ::String, ::Symbol, ::Integer
            normalized_range_ = [range_, range_]
          else
            raise Errors::RangeSpecificationError("Unrecognized range type #{range_.class}")
          end
          normalized_range_.map! do |elem_|
            if elem_.nil?
              elem_
            else
              case @type
              when :integer
                elem_.to_i
              when :string
                elem_.to_s
              when :symbol
                begin
                  elem_.to_sym
                rescue
                  raise Errors::RangeSpecificationError("Bad symbol value: #{elem_.inspect}")
                end
              end
            end
          end
          normalized_range_ << child_
          @ranges ||= Array.new
          insert_index_ = @ranges.size
          @ranges.each_with_index do |r_, i_|
            if normalized_range_[0] && r_[1]
              cmp_ = compare_values(normalized_range_[0], r_[1])
              if cmp_.nil?
                raise Errors::RangeSpecificationError
              end
              if cmp_ > 0
                next
              end
            end
            if normalized_range_[1] && r_[0]
              cmp_ = compare_values(normalized_range_[1], r_[0])
              if cmp_.nil?
                raise Errors::RangeSpecificationError
              end
              if cmp_ < 0
                insert_index_ = i_
                break
              end
            end
            raise Errors::RangeOverlapError
          end
          @ranges.insert(insert_index_, normalized_range_)
        end
      end


      # Compute descendants as a hash of names to fields, including this field.

      def _descendants_by_name  # :nodoc:
        hash_ = {@name => self}
        @children.each{ |child_| hash_.merge!(child_._descendants_by_name) }
        hash_
      end


      # Return a set of all descendant fields, including this field.

      def _descendant_fields(set_=nil)  # :nodoc:
        set_ ||= Set.new
        set_ << self
        @children.each{ |child_| child_._descendant_fields(set_) }
        set_
      end


    end


    # These methods are available in a schema field definition block.

    class FieldBuilder

      include ::Blockenspiel::DSL

      def initialize(field_, master_builder_)  # :nodoc:
        @field = field_
        @master_builder = master_builder_
      end


      # Define the given symbol.
      #
      # Recognized options include:
      #
      # <tt>:bump</tt>::
      #   The symbol to transition to when "bump" is called.
      #   Default is to remain on the same value.
      #
      # Raises Versionomy::Errors::TypeMismatchError if called when the current field
      # is not of type <tt>:symbol</tt>.
      #
      # Raises Versionomy::Errors::SymbolRedefinedError if the given symbol name is
      # already defined.

      def symbol(symbol_, opts_={})
        @field._add_symbol(symbol_, opts_)
      end


      # Provide a default value.

      def default_value(value_)
        @field._set_default_value(value_)
      end


      # Provide a "bump" procedure.
      # The given block should take a value, and return the value to transition to.
      # If you return nil, the value will remain the same.

      def to_bump(&block_)
        @field._set_bump_proc(block_)
      end


      # Provide a "compare" procedure.
      # The given block should take two values and compare them.
      # It should return a negative integer if the first is less than the second,
      # a positive integer if the first is greater than the second, or 0 if the
      # two values are equal. If the values cannot be compared, return nil.

      def to_compare(&block_)
        @field._set_compare_proc(block_)
      end


      # Provide a "canonicalize" procedure.
      # The given block should take a value and return a canonicalized value.
      # Return nil if the given value is illegal.

      def to_canonicalize(&block_)
        @field._set_canonicalize_proc(block_)
      end


      # Add a child field.
      #
      # Recognized options include:
      #
      # <tt>:only</tt>::
      #   The child should be available only for the given values of this
      #   field. See below for ways to specify this constraint.
      # <tt>:type</tt>::
      #   Type of field. This should be <tt>:integer</tt>, <tt>:string</tt>,
      #   or <tt>:symbol</tt>. Default is <tt>:integer</tt>.
      # <tt>:default_value</tt>::
      #   Default value for the field if no value is explicitly set. Default
      #   is 0 for an integer field, the empty string for a string field, or
      #   the first symbol added for a symbol field.
      #
      # You may provide an optional block. Within the block, you may call
      # methods of this class again to customize the child.
      #
      # Raises Versionomy::Errors::IllegalValueError if the given default
      # value is not legal.
      #
      # The <tt>:only</tt> constraint may be specified in one of the
      # following ways:
      #
      # * A single value (integer, string, or symbol)
      # * The result of calling range() to define an inclusive range of
      #   integers, strings, or symbols. In this case, either element may be
      #   nil, specifying an open end of the range. If the field type is
      #   symbol, the ordering of symbols for the range is defined by the
      #   order in which the symbols were added to this schema.
      # * A Range object defining a range of integers or strings.
      #   Only inclusive, not exclusive, ranges are supported.
      # * An array of the above.
      #
      # Raises Versionomy::Errors::RangeSpecificationError if the given
      # ranges are not legal.
      #
      # Raises Versionomy::Errors::RangeOverlapError if the given ranges
      # overlap previously specified ranges, or more than one default schema
      # is specified.

      def field(name_, opts_={}, &block_)
        only_ = opts_.delete(:only)
        opts_.merge!(:master_builder => @master_builder)
        @field.add_child(Schema::Field.new(name_, opts_, &block_), only_)
      end


      # Define a range for the <tt>:only</tt> parameter to +child+.
      #
      # This creates an object that +child+ interprets like a standard ruby Range. However, it
      # is customized for the use of +child+ in the following ways:
      #
      # * It supports only inclusive, not exclusive ranges.
      # * It supports open-ended ranges by setting either endpoint to nil.
      # * It supports symbol ranges under Ruby 1.8.

      def range(first_, last_)
        [first_, last_]
      end


    end


  end

end
