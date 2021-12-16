# -----------------------------------------------------------------------------
#
# Blockenspiel DSL definition
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


require 'thread'


module Blockenspiel


  # === DSL setup methods
  #
  # These class methods are available after you have included the
  # Blockenspiel::DSL module.
  #
  # By default, a class that has DSL capability will automatically make
  # all public methods available to parameterless blocks, except for the
  # +initialize+ method, any methods whose names begin with an underscore,
  # and any methods whose names end with an equals sign.
  #
  # If you want to change this behavior, use the directives defined here to
  # control exactly which methods are available to parameterless blocks.

  module DSLSetupMethods


    # :stopdoc:

    # Called when DSLSetupMethods extends a class.
    # This sets up the current class, and adds a hook that causes
    # any subclass of the current class also to be set up.

    def self.extended(klass_)
      unless klass_.instance_variable_defined?(:@_blockenspiel_module)
        _setup_class(klass_)
        def klass_.inherited(subklass_)
          ::Blockenspiel::DSLSetupMethods._setup_class(subklass_)
          super
        end
        class << klass_
          unless private_method_defined?(:_blockenspiel_default_include)
            alias_method :_blockenspiel_default_include, :include
            alias_method :include, :_blockenspiel_custom_include
          end
        end
      end
    end

    # :startdoc:


    # Set up a class.
    # Creates a DSL module for this class, optionally delegating to the superclass's module.
    # Also initializes the class's methods hash and active flag.

    def self._setup_class(klass_)  # :nodoc:
      superclass_ = klass_.superclass
      superclass_ = nil unless superclass_.respond_to?(:_get_blockenspiel_module)
      mod_ = ::Module.new
      if superclass_
        mod_.module_eval do
          include superclass_._get_blockenspiel_module
        end
      end
      klass_.instance_variable_set(:@_blockenspiel_superclass, superclass_)
      klass_.instance_variable_set(:@_blockenspiel_module, mod_)
      klass_.instance_variable_set(:@_blockenspiel_methods, {})
      klass_.instance_variable_set(:@_blockenspiel_active, nil)
    end


    # Automatically make the given method a DSL method according to the current setting.

    def _blockenspiel_auto_dsl_method(symbol_)  # :nodoc:
      if @_blockenspiel_active
        dsl_method(symbol_)
      elsif @_blockenspiel_active.nil?
        if symbol_ != :initialize && symbol_.to_s !~ /^_/ && symbol_.to_s !~ /=$/
          dsl_method(symbol_)
        end
      end
    end


    # Hook called when a method is added.
    # This calls _blockenspiel_auto_dsl_method to auto-handle the method,
    # possibly making it a DSL method according to the current setting.

    def method_added(symbol_)  # :nodoc:
      _blockenspiel_auto_dsl_method(symbol_)
      super
    end


    # Custom include method. Calls the main include implementation, but also
    # goes through the public methods of the included module and calls
    # _blockenspiel_auto_dsl_method on each to make them DSL methods
    # (possibly) according to the current setting.

    def _blockenspiel_custom_include(*modules_)  # :nodoc:
      _blockenspiel_default_include(*modules_)
      modules_.reverse_each do |mod_|
        mod_.public_instance_methods.each do |method_|
          _blockenspiel_auto_dsl_method(method_)
        end
      end
    end


    # Get this class's corresponding DSL module

    def _get_blockenspiel_module  # :nodoc:
      @_blockenspiel_module
    end


    # Get information on the given DSL method name.
    # Possible values are the name of the delegate method, false for method disabled,
    # or nil for method never defined.

    def _get_blockenspiel_delegate(name_)  # :nodoc:
      delegate_ = @_blockenspiel_methods[name_]
      if delegate_.nil? && @_blockenspiel_superclass
        @_blockenspiel_superclass._get_blockenspiel_delegate(name_)
      else
        delegate_
      end
    end


    # Make a particular method available to parameterless DSL blocks.
    #
    # To explicitly make a method available to parameterless blocks:
    #  dsl_method :my_method
    #
    # To explicitly exclude a method from parameterless blocks:
    #  dsl_method :my_method, false
    #
    # To explicitly make a method available to parameterless blocks, but
    # point it to a method of a different name on the target class:
    #  dsl_method :my_method, :target_class_method

    def dsl_method(name_, delegate_=nil)
      name_ = name_.to_sym
      if delegate_
        delegate_ = delegate_.to_sym
      elsif delegate_.nil?
        delegate_ = name_
      end
      @_blockenspiel_methods[name_] = delegate_
      unless @_blockenspiel_module.public_method_defined?(name_)
        @_blockenspiel_module.module_eval("def #{name_}(*params_, &block_); val_ = ::Blockenspiel._target_dispatch(self, :#{name_}, params_, block_); ::Blockenspiel::NO_VALUE.equal?(val_) ? super(*params_, &block_) : val_; end\n")
      end
    end


    # Control the behavior of methods with respect to parameterless blocks,
    # or make a list of methods available to parameterless blocks in bulk.
    #
    # To enable automatic exporting of methods to parameterless blocks.
    # After executing this command, all public methods defined in the class
    # will be available on parameterless blocks, until
    # <tt>dsl_methods false</tt> is called:
    #  dsl_methods true
    #
    # To disable automatic exporting of methods to parameterless blocks.
    # After executing this command, methods defined in this class will be
    # excluded from parameterless blocks, until <tt>dsl_methods true</tt>
    # is called:
    #  dsl_methods false
    #
    # To make a list of methods available to parameterless blocks in bulk:
    #  dsl_methods :my_method1, :my_method2, ...
    #
    # You can also point dsl methods to a method of a different name on the
    # target class, by using a hash syntax, as follows:
    #  dsl_methods :my_method1 => :target_class_method1,
    #              :my_method2 => :target_class_method2
    #
    # You can mix non-renamed and renamed method declarations as long as
    # the renamed (hash) methods are at the end. e.g.:
    #  dsl_methods :my_method1, :my_method2 => :target_class_method2

    def dsl_methods(*names_)
      if names_.size == 0 || names_ == [true]
        @_blockenspiel_active = true
      elsif names_ == [false]
        @_blockenspiel_active = false
      else
        if names_.last.kind_of?(::Hash)
          names_.pop.each do |name_, delegate_|
            dsl_method(name_, delegate_)
          end
        end
        names_.each do |name_|
          dsl_method(name_, name_)
        end
      end
    end


    # A DSL-friendly attr_accessor.
    #
    # This creates the usual "name" and "name=" methods in the current
    # class that can be used in the usual way. However, its implementation
    # of the "name" method (the getter) also takes an optional parameter
    # that causes it to behave as a setter. This is done because the usual
    # setter syntax cannot be used in a parameterless block, since it is
    # syntactically indistinguishable from a local variable assignment.
    # The "name" method is exposed as a dsl_method.
    #
    # For example:
    #
    #  dsl_attr_accessor :foo
    #
    # enables the following:
    #
    #  my_block do |param|
    #    param.foo = 1   # Usual setter syntax works
    #    param.foo 2     # Alternate setter syntax also works
    #    puts param.foo  # Usual getter syntax still works
    #  end
    #
    #  my_block do
    #    # foo = 1       # Usual setter syntax does NOT work since it
    #                    #   looks like a local variable assignment
    #    foo 2           # Alternate setter syntax does work
    #    puts foo        # Usual getter syntax still works
    #  end

    def dsl_attr_accessor(*names_)
      names_.each do |name_|
        unless name_.kind_of?(::String) || name_.kind_of?(::Symbol)
          raise ::TypeError, "#{name_.inspect} is not a symbol"
        end
        unless name_.to_s =~ /^[_a-zA-Z]\w+$/
          raise ::NameError, "invalid attribute name #{name_.inspect}"
        end
        module_eval("def #{name_}(value_=::Blockenspiel::NO_VALUE); ::Blockenspiel::NO_VALUE.equal?(value_) ? @#{name_} : @#{name_} = value_; end\n")
        alias_method("#{name_}=", name_)
        dsl_method(name_)
      end
    end


    # A DSL-friendly attr_writer.
    #
    # This creates the usual "name=" method in the current class that can
    # be used in the usual way. However, it also creates the method "name",
    # which also functions as a setter (but not a getter). This is done
    # because the usual setter syntax cannot be used in a parameterless
    # block, since it is syntactically indistinguishable from a local
    # variable assignment. The "name" method is exposed as a dsl_method.
    #
    # For example:
    #
    #  dsl_attr_writer :foo
    #
    # is functionally equivalent to:
    #
    #  attr_writer :foo
    #  alias_method :foo, :foo=
    #  dsl_method :foo
    #
    # which enables the following:
    #
    #  my_block do |param|
    #    param.foo = 1   # Usual setter syntax works
    #    param.foo 2     # Alternate setter syntax also works
    #  end
    #  my_block do
    #    # foo = 1       # Usual setter syntax does NOT work since it
    #                    #   looks like a local variable assignment
    #    foo(2)          # Alternate setter syntax does work
    #  end

    def dsl_attr_writer(*names_)
      names_.each do |name_|
        attr_writer(name_)
        alias_method(name_, "#{name_}=")
        dsl_method(name_)
      end
    end


  end


  # === DSL activation module
  #
  # Include this module in a class to mark this class as a DSL class and
  # make it possible for its methods to be called from a block that does not
  # take a parameter.
  #
  # After you include this module, you can use the directives defined in
  # DSLSetupMethods to control what methods are available to DSL blocks
  # that do not take parameters.

  module DSL

    def self.included(klass_)  # :nodoc:
      unless klass_.kind_of?(::Class)
        raise ::Blockenspiel::BlockenspielError, "You cannot include Blockenspiel::DSL in a module (yet)"
      end
      klass_.extend(::Blockenspiel::DSLSetupMethods)
    end

  end


  # === DSL activation base class
  #
  # Subclasses of this base class are considered DSL classes.
  # Methods of the class can be made available to be called from a block that
  # doesn't take an explicit block parameter.
  # You may use the directives defined in DSLSetupMethods to control how
  # methods of the class are handled in such blocks.
  #
  # Subclassing this base class is functionally equivalent to simply
  # including Blockenspiel::DSL in the class.

  class Base

    include ::Blockenspiel::DSL

  end


end
