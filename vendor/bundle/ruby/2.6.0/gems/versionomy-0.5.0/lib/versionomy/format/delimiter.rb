# -----------------------------------------------------------------------------
#
# Versionomy delimiter format
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

  module Format


    # The Delimiter format class provides a DSL for building formats that
    # can handle most cases where the fields of a version number appear
    # consecutively in order in the string formatting. We expect most
    # version number schemes should fall into this category.
    #
    # In general, the strategy is to provide, for each field, a set of
    # regular expressions that recognize different formats for that field.
    # Every field must be of the form "(pre)(value)(post)"
    # where (pre) and (post) are delimiters preceding and
    # following the value. Either or both delimiters may be the empty string.
    #
    # To parse a string, the string is scanned from left to right and
    # matched against the format for the fields in order. If the string
    # matches, that part of the string is consumed and the field value is
    # interpreted from it. If the string does not match, and the field is
    # not marked as "required", then the field is set to its default value
    # and the next field is tried.
    #
    # During parsing, the actual delimiters, along with other information
    # such as whether or not fields are required, are saved into a default
    # set of parameters for unparsing. These are saved in the unparse_params
    # of the version value, so that the version number can be unparsed in
    # generally the same form. If the version number value is modified, this
    # allows the unparsing of the new value to generally follow the format
    # of the original string.
    #
    # Formats that use the Delimiter mechanism also provide support for
    # certain parsing and unparsing parameters. See the documentation for
    # the parse and unparse methods for details.
    #
    # For a usage example, see the definition of the standard format in
    # Versionomy::Format::Standard#create.

    class Delimiter < Base


      # Create a format using delimiter tools.
      # You should provide the version number schema, a set of default
      # options, and a block.
      #
      # Within the block, you can call methods of
      # Versionomy::Format::Delimiter::Builder
      # to provide parsers for the fields of the schema. Any fields you do
      # not explicitly configure will get parsed in a default manner.

      def initialize(schema_, default_opts_={}, &block_)
        # Special case used by modified_copy
        if schema_.kind_of?(Delimiter)
          orig_ = schema_
          @schema = orig_.schema
          @default_parse_params = orig_.default_parse_params
          @default_unparse_params = orig_.default_unparse_params
          @field_handlers = orig_.instance_variable_get(:@field_handlers).dup
          builder_ = Delimiter::Builder.new(@schema, @field_handlers,
            @default_parse_params, @default_unparse_params)
          ::Blockenspiel.invoke(block_, builder_)
          return
        end

        @schema = schema_
        @field_handlers = {}
        @default_parse_params = {}
        @default_unparse_params = {}
        builder_ = Delimiter::Builder.new(@schema, @field_handlers,
          @default_parse_params, @default_unparse_params)
        ::Blockenspiel.invoke(block_, builder_)
        _interpret_field_lists(@default_unparse_params)
        @schema.names.each do |name_|
          @field_handlers[name_] ||= Delimiter::FieldHandler.new(@schema.field_named(name_), default_opts_)
        end
      end


      # Returns the schema understood by this format.
      # This method is required by the Format contract.

      def schema
        @schema
      end


      # Parse the given string and return a value.
      # This method is required by the Format contract.
      #
      # This method provides, out of the box, support for the following
      # parse parameters:
      #
      # <tt>:extra_characters</tt>::
      #   Determines what to do if the entire string cannot be consumed by
      #   the parsing process. If set to <tt>:ignore</tt>, any extra
      #   characters are ignored. If set to <tt>:suffix</tt>, the extra
      #   characters are set as the <tt>:suffix</tt> unparse parameter and
      #   are thus appended to the end of the string when unparsing takes
      #   place. If set to <tt>:error</tt> (the default), causes a
      #   Versionomy::Errors::ParseError to be raised if there are
      #   uninterpreted characters.

      def parse(string_, params_=nil)
        parse_params_ = default_parse_params
        parse_params_.merge!(params_) if params_
        parse_state_ = {
          :backtrack => nil,
          :string => string_,
          :values => {},
          :unparse_params => {},
          :field => @schema.root_field,
          :recognizer_index => 0,
          :previous_field_missing => false
        }
        while (field_ = parse_state_[:field])
          handler_ = @field_handlers[field_.name]
          recognizer_ = handler_.get_recognizer(parse_state_[:recognizer_index])
          parse_data_ = nil
          if recognizer_
            parse_state_[:recognizer_index] += 1
            parse_data_ = recognizer_.parse(parse_state_, parse_params_)
            if parse_data_
              parse_state_[:previous_field_missing] = false
              if recognizer_.requires_next_field
                parse_state_ = {
                  :backtrack => parse_state_,
                  :string => parse_state_[:string],
                  :values => parse_state_[:values].dup,
                  :unparse_params => parse_state_[:unparse_params].dup,
                  :field => parse_state_[:field],
                  :recognizer_index => 0,
                  :previous_field_missing => false,
                  :next_field_required => true,
                }
              else
                parse_state_[:next_field_required] = false
              end
            end
          elsif parse_state_[:next_field_required]
            parse_state_ = parse_state_[:backtrack]
          else
            parse_data_ = [handler_.default_value, nil, nil, nil]
            parse_state_[:previous_field_missing] = true
            parse_state_[:next_field_required] = false
          end
          if parse_data_
            parse_state_[:values][field_.name] = parse_data_[0]
            parse_state_[:string] = parse_data_[2] if parse_data_[2]
            parse_state_[:unparse_params].merge!(parse_data_[3]) if parse_data_[3]
            parse_state_[:field] = field_.child(parse_data_[0])
            parse_state_[:recognizer_index] = 0
            handler_.set_style_unparse_param(parse_data_[1], parse_state_[:unparse_params])
          end
        end
        unparse_params_ = parse_state_[:unparse_params]
        if parse_state_[:string].length > 0
          case parse_params_[:extra_characters]
          when :ignore
            # do nothing
          when :suffix
            unparse_params_[:suffix] = parse_state_[:string]
          else
            raise Errors::ParseError, "Extra characters: #{parse_state_[:string].inspect}"
          end
        end
        Value.new(parse_state_[:values], self, unparse_params_)
      end


      # Unparse the given value and return a string.
      # This method is required by the Format contract.
      #
      # This method provides, out of the box, support for the following
      # unparse parameters:
      #
      # <tt>:suffix</tt>::
      #   A string to append to the unparsed string. Default is nothing.
      # <tt>:required_fields</tt>::
      #   An array of field names that must be present in the unparsed
      #   string. These are generally fields with default_value_optional
      #   set, but that we want present in the string anyway. For example,
      #   in the version number "2.0.0", often the third field will be
      #   default_value_optional, but we can include it in the required
      #   fields passed to unparse to force it to appear in the string.
      # <tt>:optional_fields</tt>::
      #   An array of field names that should have their presence in
      #   required_fields undone.
      # <tt>:<i>fieldname</i>_required</tt>::
      #   This is an alternate way of specifying whether a potentially
      #   optional field should be required. Accepted values are true
      #   and false.
      # <tt>:<i>fieldname</i>_style</tt>::
      #   Specify the style for unparsing the given field. See
      #   Versionomy::Format::Delimiter::Builder#field for more
      #   discussion of styles.
      # <tt>:<i>fieldname</i>_delim</tt>::
      #   Set the pre-delimiter for the given field, if supported.
      #   Note that the string specified must be legal-- it must match the
      #   regexp for the field. If not, it will revert to the default.
      # <tt>:<i>fieldname</i>_postdelim</tt>::
      #   Set the post-delimiter for the given field, if supported.
      #   Note that the string specified must be legal-- it must match the
      #   regexp for the field. If not, it will revert to the default.
      # <tt>:<i>fieldname</i>_case</tt>::
      #   This is used by letter-formatted integer fields only, and
      #   sets the case to use while unparsing. Recognized values are
      #   <tt>:lower</tt> (the default), and <tt>:upper</tt>.

      def unparse(value_, params_=nil)
        unparse_params_ = value_.unparse_params || default_unparse_params
        _interpret_field_lists(unparse_params_)
        if params_
          unparse_params_.merge!(params_)
          _interpret_field_lists(unparse_params_)
        end
        skipped_handler_list_ = nil
        requires_next_field_ = false
        string_ = ''
        value_.each_field_object do |field_, val_|
          handler_ = @field_handlers[field_.name]
          unparse_data_ = handler_.unparse(val_, unparse_params_, requires_next_field_)
          if unparse_data_
            if skipped_handler_list_ && handler_.requires_previous_field
              skipped_handler_list_.each do |pair_|
                frag_ = pair_[0].unparse(pair_[1], unparse_params_, true)
                unless frag_
                  raise Errors::UnparseError, "Field #{field_.name} empty although a prerequisite for a later field"
                end
                string_ << frag_[0]
              end
            end
            skipped_handler_list_ = nil
            string_ << unparse_data_[0]
            requires_next_field_ = unparse_data_[1]
          else
            if handler_.requires_previous_field
              (skipped_handler_list_ ||= []) << [handler_, val_]
            else
              skipped_handler_list_ = [[handler_, val_]]
            end
            requires_next_field_ = false
          end
        end
        string_ << (unparse_params_[:suffix] || '')
        string_
      end


      # Return a copy of the default parsing parameters used by this format.
      # This hash cannot be edited in place. To modify the default parsing
      # parameters, use modified_copy and call
      # Versionomy::Format::Delimiter::Builder#default_parse_params in the block.

      def default_parse_params
        @default_parse_params.dup
      end


      # Return a copy of the default unparsing parameters used by this format.
      # This hash cannot be edited in place. To modify the default unparsing
      # parameters, use modified_copy and call
      # Versionomy::Format::Delimiter::Builder#default_unparse_params in the block.

      def default_unparse_params
        @default_unparse_params.dup
      end


      # Create a copy of this format, with the modifications given in the
      # provided block. You can call methods of Versionomy::Format::Delimiter::Builder
      # in the block. Field handlers that you specify in that block will
      # override and change the field handlers from the original. Any fields
      # not specified in this block will use the handlers from the original.

      def modified_copy(&block_)
        Delimiter.new(self, &block_)
      end


      # A utility method that interprets required_fields and
      # optional_fields parameters.

      def _interpret_field_lists(unparse_params_)  # :nodoc:
        fields_ = unparse_params_.delete(:required_fields)
        if fields_
          fields_ = [fields_] unless fields_.kind_of?(::Array)
          fields_.each do |f_|
            unparse_params_["#{f_}_required".to_sym] = true
          end
        end
        fields_ = unparse_params_.delete(:optional_fields)
        if fields_
          fields_ = [fields_] unless fields_.kind_of?(::Array)
          fields_.each do |f_|
            unparse_params_["#{f_}_required".to_sym] = false
          end
        end
      end
      private :_interpret_field_lists


      # This class defines methods that you can call within the DSL block
      # passed to Versionomy::Format::Delimiter#new.
      #
      # Generally, you call the field method of this class a number of times
      # to define the formatting for each field.

      class Builder

        include ::Blockenspiel::DSL

        def initialize(schema_, field_handlers_, default_parse_params_, default_unparse_params_)  # :nodoc:
          @schema = schema_
          @field_handlers = field_handlers_
          @default_parse_params = default_parse_params_
          @default_unparse_params = default_unparse_params_
        end


        # Specify how to handle a given field.
        # You must pass the name of the field, a hash of options, and a
        # block defining the handling of the field.
        #
        # Within the block, you set up "recognizers" for various regular
        # expression patterns. These recognizers are tested in order when
        # parsing a version number.
        #
        # The methods that can be called from the block are determined by
        # the type of field. If the field is an integer field, the methods
        # of Versionomy::Format::Delimiter::IntegerFieldBuilder can be
        # called from the block. If the field is a string field, the methods
        # of Versionomy::Format::Delimiter::StringFieldBuilder can be
        # called. If the field is a symbolic field, the methods of
        # Versionomy::Format::Delimiter::SymbolFieldBuilder can be called.
        #
        # === Options
        #
        # The opts hash includes a number of options that control how the
        # field is parsed.
        #
        # Some of these are regular expressions that indicate what patterns
        # are recognized by the parser. Regular expressions should be passed
        # in as the string representation of the regular expression, not a
        # Regexp object itself. For example, use the string '-' rather than
        # the Regexp /-/ to recognize a hyphen delimiter.
        #
        # The following options are recognized:
        #
        # <tt>:default_value_optional</tt>::
        #   If set to true, this the field may be omitted in the unparsed
        #   (formatted) version number, if the value is the default value
        #   for this field. However, if the following field is present and
        #   set as <tt>:requires_previous_field</tt>, then this field is
        #   still unparsed even if it is its default value.
        #   For example, for a version number like "2.0.0", often the third
        #   field is optional, but the first and second are required, so it
        #   will often be unparsed as "2.0".
        #   Default is false.
        # <tt>:default_value</tt>::
        #   The actual value set for this field if it is omitted from the
        #   version string. Defaults to the field's schema default value,
        #   but that can be overridden here.
        # <tt>:case_sensitive</tt>::
        #   If set to true, the regexps are case-sensitive. Default is false.
        # <tt>:delimiter_regexp</tt>::
        #   The regular expression string for the pre-delimiter. This pattern
        #   must appear before the current value in the string, and is
        #   consumed when the field is parsed, but is not part of the value
        #   itself. Default is '\.' to recognize a period.
        # <tt>:post_delimiter_regexp</tt>::
        #   The regular expression string for the post-delimiter. This pattern
        #   must appear before the current value in the string, and is
        #   consumed when the field is parsed, but is not part of the value
        #   itself. Default is '' to indicate no post-delimiter.
        # <tt>:expected_follower_regexp</tt>::
        #   The regular expression string for what characters are expected to
        #   follow this field in the string. These characters are not part
        #   of the field itself, and are *not* consumed when the field is
        #   parsed; however, they must be present immediately after this
        #   field in order for the field to be recognized. Default is '' to
        #   indicate that we aren't testing for any particular characters.
        # <tt>:default_delimiter</tt>::
        #   The default delimiter string. This is the string that is used
        #   to unparse a field value if the field was not present when the
        #   value was originally parsed. For example, if you parse the string
        #   "2.0", bump the tiny version so that the value is "2.0.1", and
        #   unparse, the unparsing won't receive the second period from
        #   parsing the original string, so its delimiter will use the default.
        #   Default value is '.'
        # <tt>:default_post_delimiter</tt>::
        #   The default post-delimiter string. Default value is '' indicating
        #   no post-delimiter.
        # <tt>:requires_previous_field</tt>::
        #   If set to true, this field's presence in a formatted version string
        #   requires the presence of the previous field. For example, in a
        #   typical version number "major.minor.tiny", tiny should appear in
        #   the string only if minor also appears, so tiny should have this
        #   parameter set to true. The default is true, so you must specify
        #   <tt>:requires_previous_field => false</tt> explicitly if you want
        #   a field not to require the previous field.
        # <tt>:requires_next_field</tt>::
        #   If set to true, this field's presence in a formatted version
        #   string requires the presence of the next field. For example, in
        #   the version "1.0a5", the release_type field requires the presence
        #   of the alpha_version field, because if the "5" was missing, the
        #   string "1.0a" looks like a patchlevel indicator. Often it is
        #   easier to set default_value_optional in the next field, but this
        #   option is also available if the behavior is dependent on the
        #   value of this previous field.
        # <tt>:default_style</tt>::
        #   The default style for this field. This is the style used for
        #   unparsing if the value was not constructed by a parser or is
        #   otherwise missing the style for this field.
        #
        # === Styles
        #
        # A field may have different representation "styles". For example,
        # you could represent a patchlevel of 1 as "1.0-1" or "1.0a".
        # When a version number string is parsed, the parser and unparser
        # work together to remember which style was parsed, and that style
        # is used when the version number is unparsed.
        #
        # Specify styles as options to the calls made within the block that
        # is passed to this method. In the above case, you could define the
        # patchlevel field with a block that has two calls, one that uses
        # Delimiter::IntegerFieldBuilder#recognize_number and passes the
        # option <tt>:style => :number</tt>, and another that uses
        # Delimiter::IntegerFieldBuilder#recognize_letter and passes the
        # option <tt>:style => :letter</tt>.
        #
        # The standard format uses styles to preserve the different
        # syntaxes for the release_type field. See the source code in
        # Versionomy::Format::Standard#create for this example.

        def field(name_, opts_={}, &block_)
          name_ = name_.to_sym
          field_ = @schema.field_named(name_)
          if !field_
            raise Errors::FormatCreationError, "Unknown field name #{name_.inspect}"
          end
          @field_handlers[name_] = Delimiter::FieldHandler.new(field_, opts_, &block_)
        end


        # Set or modify the default parameters used when parsing a value.

        def default_parse_params(params_)
          @default_parse_params.merge!(params_)
        end


        # Set or modify the default parameters used when unparsing a value.

        def default_unparse_params(params_)
          @default_unparse_params.merge!(params_)
        end

      end


      # This class defines methods that can be called from the block passed
      # to Versionomy::Format::Delimiter::Builder#field if the field is
      # of integer type.

      class IntegerFieldBuilder

        include ::Blockenspiel::DSL

        def initialize(recognizers_, field_, default_opts_)  # :nodoc:
          @recognizers = recognizers_
          @field = field_
          @default_opts = default_opts_
        end


        # Recognize a numeric-formatted integer field.
        # Using the opts parameter, you can override any of the field's
        # overall parsing options. You may also set the following additional
        # options:
        #
        # <tt>:strip_leading_zeros</tt>::
        #   If false (the default), and a value has leading zeros, it is
        #   assumed that the field has a minimum width, and unparsing will
        #   always pad left with zeros to reach that minimum width. If set
        #   to true, leading zeros are stripped from a value, and this
        #   padding is never done.

        def recognize_number(opts_={})
          @recognizers << Delimiter::BasicIntegerRecognizer.new(@field, @default_opts.merge(opts_))
        end


        # Recognize a letter-formatted integer field. That is, the value is
        # formatted as an alphabetic letter where "a" represents 1, up to
        # "z" representing 26.
        #
        # Using the opts parameter, you can override any of the field's
        # overall parsing options. You may also set the following additional
        # options:
        #
        # <tt>:case</tt>::
        #   Case-sensitivity of the letter. Possible values are
        #   <tt>:upper</tt>, <tt>:lower</tt>, and <tt>:either</tt>.
        #   Default is <tt>:either</tt>.

        def recognize_letter(opts_={})
          @recognizers << Delimiter::AlphabeticIntegerRecognizer.new(@field, @default_opts.merge(opts_))
        end

      end


      # This class defines methods that can be called from the block passed
      # to Versionomy::Format::Delimiter::Builder#field if the field is
      # of string type.

      class StringFieldBuilder

        include ::Blockenspiel::DSL

        def initialize(recognizers_, field_, default_opts_)  # :nodoc:
          @recognizers = recognizers_
          @field = field_
          @default_opts = default_opts_
        end


        # Recognize a string field whose value matches a regular expression.
        # The regular expression must be passed as a string. E.g. use
        # "[a-z]+" instead of /[a-z]+/.
        # Using the opts parameter, you can override any of the field's
        # overall parsing options.

        def recognize_regexp(regexp_, opts_={})
          @recognizers << Delimiter::RegexpStringRecognizer.new(@field, regexp_, @default_opts.merge(opts_))
        end

      end


      # This class defines methods that can be called from the block passed
      # to Versionomy::Format::Delimiter::Builder#field if the field is
      # of symbolic type.

      class SymbolFieldBuilder

        include ::Blockenspiel::DSL

        def initialize(recognizers_, field_, default_opts_)  # :nodoc:
          @recognizers = recognizers_
          @field = field_
          @default_opts = default_opts_
        end


        # Recognize a symbolic value represented by a particular regular
        # expression. The regular expression must be passed as a string.
        # E.g. use "[a-z]+" instead of /[a-z]+/.
        # The "canonical" parameter indicates the canonical syntax for the
        # value, for use in unparsing.
        #
        # Using the opts parameter, you can override any of the field's
        # overall parsing options.

        def recognize_regexp(value_, regexp_, canonical_, opts_={}, &block_)
          @recognizers << Delimiter::RegexpSymbolRecognizer.new(@field, value_, regexp_, canonical_, @default_opts.merge(opts_))
        end


        # Recognize a set of symbolic values, each represented by a
        # particular regular expression, but all sharing the same delimiters
        # and options. Use this instead of repeated calls to recognize_regexp
        # for better performance.
        #
        # Using the opts parameter, you can override any of the field's
        # overall parsing options.
        #
        # In the block, you should call methods of
        # Versionomy::Format::Delimiter::MappingSymbolBuilder to map values
        # to regular expression representations.

        def recognize_regexp_map(opts_={}, &block_)
          @recognizers << Delimiter::MappingSymbolRecognizer.new(@field, @default_opts.merge(opts_), &block_)
        end

      end


      # Methods in this class can be called from the block passed to
      # Versionomy::Format::Delimiter::SymbolFieldBuilder#recognize_regexp_map
      # to define the mapping between the values of a symbolic field and
      # the string representations of those values.

      class MappingSymbolBuilder

        include ::Blockenspiel::DSL

        def initialize(mappings_in_order_, mappings_by_value_)  # :nodoc:
          @mappings_in_order = mappings_in_order_
          @mappings_by_value = mappings_by_value_
        end


        # Map a value to a string representation.
        # The optional regexp field, if specified, provides a regular
        # expression pattern for matching the value representation. If it
        # is omitted, the representation is used as the regexp.

        def map(value_, representation_, regexp_=nil)
          regexp_ ||= representation_
          array_ = [regexp_, representation_, value_]
          @mappings_by_value[value_] ||= array_
          @mappings_in_order << array_
        end

      end


      # This class handles the parsing and unparsing of a single field.
      # It manages an ordered list of recognizers, each understanding a
      # particular syntax. These recognizers are checked in order when
      # parsing and unparsing.

      class FieldHandler  # :nodoc:


        # Creates a FieldHandler, using a DSL block appropriate to the
        # field type to configure the recognizers.

        def initialize(field_, default_opts_={}, &block_)
          @field = field_
          @recognizers = []
          @requires_previous_field = default_opts_.fetch(:requires_previous_field, true)
          @default_value = default_opts_[:default_value] || field_.default_value
          @default_style = default_opts_.fetch(:default_style, nil)
          @style_unparse_param_key = "#{field_.name}_style".to_sym
          if block_
            builder_ = case field_.type
              when :integer
                Delimiter::IntegerFieldBuilder.new(@recognizers, field_, default_opts_)
              when :string
                Delimiter::StringFieldBuilder.new(@recognizers, field_, default_opts_)
              when :symbol
                Delimiter::SymbolFieldBuilder.new(@recognizers, field_, default_opts_)
            end
            ::Blockenspiel.invoke(block_, builder_)
          end
        end


        # Returns true if this field can appear in an unparsed string only
        # if the previous field also appears.

        def requires_previous_field
          @requires_previous_field
        end


        # Returns the default value set when this field is missing from a
        # version string.

        def default_value
          @default_value
        end


        # Gets the given indexed recognizer. Returns nil if the index is out
        # of range.

        def get_recognizer(index_)
          @recognizers[index_]
        end


        # Finishes up parsing by setting the appropriate style field in the
        # unparse_params, if needed.

        def set_style_unparse_param(style_, unparse_params_)
          if style_ && style_ != @default_style
            unparse_params_[@style_unparse_param_key] = style_
          end
        end


        # Unparse a string from this field value.
        # This may return nil if this field is not required.

        def unparse(value_, unparse_params_, required_for_later_)
          style_ = unparse_params_[@style_unparse_param_key] || @default_style
          @recognizers.each do |recog_|
            if recog_.should_unparse?(value_, style_)
              fragment_ = recog_.unparse(value_, style_, unparse_params_, required_for_later_)
              return fragment_ ? [fragment_, recog_.requires_next_field] : nil
            end
          end
          required_for_later_ ? ['', false] : nil
        end

      end


      # A recognizer handles both parsing and unparsing of a particular kind
      # of syntax. During parsing, it recognizes the syntax based on regular
      # expressions for the delimiters and the value. If the string matches
      # the syntax recognized by this object, an appropriate value and style
      # are returned. During unparsing, the should_unparse? method should be
      # called first to determine whether this object is responsible for
      # unparsing the given value and style. If should_unparse? returns
      # true, the unparse method should be called to actually generate a
      # a string fragment, or return nil if the field is determined to be
      # optional in the unparsed string.
      #
      # This is a base class. The actual classes should implement
      # initialize, parsed_value, and unparsed_value, and may optionally
      # override the should_unparse? method.

      class RecognizerBase  # :nodoc:

        # Derived classes should call this from their initialize method
        # to set up the recognizer's basic parameters.

        def setup(field_, value_regexp_, opts_)
          @style = opts_[:style]
          @default_value_optional = opts_[:default_value_optional]
          @default_value = opts_[:default_value] || field_.default_value
          @regexp_options = opts_[:case_sensitive] ? nil : ::Regexp::IGNORECASE
          @value_regexp = ::Regexp.new("\\A(#{value_regexp_})", @regexp_options)
          regexp_ = opts_[:delimiter_regexp] || '\.'
          @delimiter_regexp = regexp_.length > 0 ? ::Regexp.new("\\A(#{regexp_})", @regexp_options) : nil
          @full_delimiter_regexp = regexp_.length > 0 ? ::Regexp.new("\\A(#{regexp_})\\z", @regexp_options) : nil
          regexp_ = opts_[:post_delimiter_regexp] || ''
          @post_delimiter_regexp = regexp_.length > 0 ? ::Regexp.new("\\A(#{regexp_})", @regexp_options) : nil
          @full_post_delimiter_regexp = regexp_.length > 0 ? ::Regexp.new("\\A(#{regexp_})\\z", @regexp_options) : nil
          regexp_ = opts_[:expected_follower_regexp] || ''
          @follower_regexp = regexp_.length > 0 ? ::Regexp.new("\\A(#{regexp_})", @regexp_options) : nil
          @default_delimiter = opts_[:default_delimiter] || '.'
          @default_post_delimiter = opts_[:default_post_delimiter] || ''
          @requires_previous_field = opts_.fetch(:requires_previous_field, true)
          @requires_next_field = opts_.fetch(:requires_next_field, false)
          name_ = field_.name
          @delim_unparse_param_key = "#{name_}_delim".to_sym
          @post_delim_unparse_param_key = "#{name_}_postdelim".to_sym
          @required_unparse_param_key = "#{name_}_required".to_sym
        end


        # Attempt to parse the field from the string if the syntax matches
        # this recognizer's configuration.
        # Returns either nil, indicating that this recognizer doesn't match
        # the given syntax, or a two element array of the value and style.

        def parse(parse_state_, parse_params_)
          return nil if @requires_previous_field && parse_state_[:previous_field_missing]
          string_ = parse_state_[:string]
          if @delimiter_regexp
            match_ = @delimiter_regexp.match(string_)
            return nil unless match_
            delim_ = match_[0]
            string_ = match_.post_match
          else
            delim_ = ''
          end
          match_ = @value_regexp.match(string_)
          return nil unless match_
          value_ = match_[0]
          string_ = match_.post_match
          if @post_delimiter_regexp
            match_ = @post_delimiter_regexp.match(string_)
            return nil unless match_
            post_delim_ = match_[0]
            string_ = match_.post_match
          else
            post_delim_ = nil
          end
          if @follower_regexp
            match_ = @follower_regexp.match(string_)
            return nil unless match_
          end
          parse_result_ = parsed_value(value_, parse_params_)
          return nil unless parse_result_
          unparse_params_ = parse_result_[1] || {}
          if delim_ != @default_delimiter
            unparse_params_[@delim_unparse_param_key] = delim_
          end
          if post_delim_ && post_delim_ != @default_post_delimiter
            unparse_params_[@post_delim_unparse_param_key] = post_delim_
          end
          unparse_params_[@required_unparse_param_key] = true if @default_value_optional
          [parse_result_[0], @style, string_, unparse_params_]
        end


        # Returns true if this field can appear in an unparsed string only
        # if the next field also appears.

        def requires_next_field
          @requires_next_field
        end


        # Returns true if this recognizer should be used to unparse the
        # given value and style.

        def should_unparse?(value_, style_)
          style_ == @style
        end


        # Unparse the given value in the given style, and return a string
        # fragment, or nil if the field is determined to be "optional" to
        # unparse and isn't otherwise required (because a later field needs
        # it to be present, for example).
        #
        # It is guaranteed that this will be called only if should_unparse?
        # returns true.

        def unparse(value_, style_, unparse_params_, required_for_later_)
          str_ = nil
          if !@default_value_optional || value_ != @default_value ||
              required_for_later_ || unparse_params_[@required_unparse_param_key]
          then
            str_ = unparsed_value(value_, style_, unparse_params_)
            if str_
              if !@full_delimiter_regexp
                delim_ = ''
              else
                delim_ = unparse_params_[@delim_unparse_param_key] || @default_delimiter
                if @full_delimiter_regexp !~ delim_
                  delim_ = @default_delimiter
                end
              end
              if !@full_post_delimiter_regexp
                post_delim_ = ''
              else
                post_delim_ = unparse_params_[@post_delim_unparse_param_key] || @default_post_delimiter
                if @full_post_delimiter_regexp !~ post_delim_
                  post_delim_ = @default_post_delimiter
                end
              end
              str_ = delim_ + str_ + post_delim_
            end
            str_
          else
            nil
          end
        end

      end


      # A recognizer for a numeric integer field

      class BasicIntegerRecognizer < RecognizerBase  #:nodoc:

        def initialize(field_, opts_={})
          @strip_leading_zeros = opts_[:strip_leading_zeros]
          @width_unparse_param_key = "#{field_.name}_width".to_sym
          setup(field_, '\d+', opts_)
        end

        def parsed_value(value_, parse_params_)
          if !@strip_leading_zeros && value_ =~ /^0\d/
            [value_.to_i, {@width_unparse_param_key => value_.length}]
          else
            [value_.to_i, nil]
          end
        end

        def unparsed_value(value_, style_, unparse_params_)
          if !@strip_leading_zeros && (width_ = unparse_params_[@width_unparse_param_key])
            "%0#{width_.to_i}d" % value_
          else
            value_.to_s
          end
        end

      end


      # A recognizer for an alphabetic integer field. Such a field
      # represents values 1-26 as letters of the English alphabet.

      class AlphabeticIntegerRecognizer < RecognizerBase  # :nodoc:

        def initialize(field_, opts_={})
          @case_unparse_param_key = "#{field_.name}_case".to_sym
          @case = opts_[:case]
          case @case
          when :upper
            value_regexp_ = '[A-Z]'
          when :lower
            value_regexp_ = '[a-z]'
          else #either
            value_regexp_ = '[a-zA-Z]'
          end
          setup(field_, value_regexp_, opts_)
        end

        def parsed_value(value_, parse_params_)
          value_ = value_.unpack('c')[0]  # Compatible with both 1.8 and 1.9
          if value_ >= 97 && value_ <= 122
            [value_ - 96, {@case_unparse_param_key => :lower}]
          elsif value_ >= 65 && value_ <= 90
            [value_ - 64, {@case_unparse_param_key => :upper}]
          else
            [0, nil]
          end
        end

        def unparsed_value(value_, style_, unparse_params_)
          if value_ >= 1 && value_ <= 26
            if unparse_params_[@case_unparse_param_key] == :upper
              (value_+64).chr
            else
              (value_+96).chr
            end
          else
            value_.to_s
          end
        end

      end


      # A recognizer for strings that match a particular given regular
      # expression, for use in string-valued fields.

      class RegexpStringRecognizer < RecognizerBase  # :nodoc:

        def initialize(field_, regexp_='[a-zA-Z0-9]+', opts_={})
          setup(field_, regexp_, opts_)
        end

        def parsed_value(value_, parse_params_)
          [value_, nil]
        end

        def unparsed_value(value_, style_, unparse_params_)
          value_.to_s
        end

      end


      # A recognizer for symbolic fields that recognizes a single regular
      # expression and maps it to a single particular value.

      class RegexpSymbolRecognizer < RecognizerBase  # :nodoc:

        def initialize(field_, value_, regexp_, canonical_, opts_={})
          setup(field_, regexp_, opts_)
          @value = value_
          @canonical = canonical_
        end

        def parsed_value(value_, parse_params_)
          [@value, nil]
        end

        def unparsed_value(value_, style_, unparse_params_)
          @canonical
        end

        def should_unparse?(value_, style_)
          style_ == @style && value_ == @value
        end

      end


      # A recognizer for symbolic fields that recognizes a mapping of values
      # to regular expressions.

      class MappingSymbolRecognizer < RecognizerBase  # :nodoc:

        def initialize(field_, opts_={}, &block_)
          @mappings_in_order = []
          @mappings_by_value = {}
          builder_ = Delimiter::MappingSymbolBuilder.new(@mappings_in_order, @mappings_by_value)
          ::Blockenspiel.invoke(block_, builder_)
          regexps_ = @mappings_in_order.map{ |map_| "(#{map_[0]})" }
          setup(field_, regexps_.join('|'), opts_)
          @mappings_in_order.each do |map_|
            map_[0] = ::Regexp.new("\\A(#{map_[0]})", @regexp_options)
          end
        end

        def parsed_value(value_, parse_params_)
          @mappings_in_order.each do |map_|
            return [map_[2], nil] if map_[0].match(value_)
          end
          nil
        end

        def unparsed_value(value_, style_, unparse_params_)
          @mappings_by_value[value_][1]
        end

        def should_unparse?(value_, style_)
          style_ == @style && @mappings_by_value.include?(value_)
        end

      end


    end


  end

end
