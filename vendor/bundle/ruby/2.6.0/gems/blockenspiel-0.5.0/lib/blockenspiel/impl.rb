# -----------------------------------------------------------------------------
#
# Blockenspiel implementation
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


  # === Determine whether the mixin strategy is available
  #
  # Returns true if the mixin strategy is available on the current ruby
  # platform. This will be false for most platforms.

  def self.mixin_available?
    !::Blockenspiel::Unmixer.const_defined?(:UNIMPLEMENTED)
  end


  # === Invoke a given DSL
  #
  # This is the entry point for Blockenspiel. Call this function to invoke
  # a set of DSL code provided by the user of your API.
  #
  # For example, if you want users of your API to be able to do this:
  #
  #  call_dsl do
  #    foo(1)
  #    bar(2)
  #  end
  #
  # Then you should implement <tt>call_dsl</tt> like this:
  #
  #  def call_dsl(&block)
  #    my_dsl = create_block_implementation
  #    Blockenspiel.invoke(block, my_dsl)
  #    do_something_with(my_dsl)
  #  end
  #
  # In the above, <tt>create_block_implementation</tt> is a placeholder that
  # returns an instance of your DSL methods class. This class includes the
  # Blockenspiel::DSL module and defines the DSL methods +foo+ and +bar+.
  # See Blockenspiel::DSLSetupMethods for a set of tools you can use in your
  # DSL methods class for creating a DSL.
  #
  # === Usage patterns
  #
  # The invoke method has a number of forms, depending on whether the API
  # user's DSL code is provided as a block or a string, and depending on
  # whether the DSL methods are specified statically using a DSL class or
  # dynamically using a block.
  #
  # [<tt>Blockenspiel.invoke(<i>user_block</i>, <i>my_dsl</i>, <i>opts</i>)</tt>]
  #   This form takes the user's code as a block, and the DSL itself as an
  #   object with DSL methods. The opts hash is optional and provides a
  #   set of arguments as described below under "Block DSL options".
  #
  # [<tt>Blockenspiel.invoke(<i>user_block</i>, <i>opts</i>) { ... }</tt>]
  #   This form takes the user's code as a block, while the DSL itself is
  #   specified in the given block, as described below under "Dynamic
  #   target generation". The opts hash is optional and provides a set of
  #   arguments as described below under "Block DSL options".
  #
  # [<tt>Blockenspiel.invoke(<i>user_string</i>, <i>my_dsl</i>, <i>opts</i>)</tt>]
  #   This form takes the user's code as a string, and the DSL itself as an
  #   object with DSL methods. The opts hash is optional and provides a
  #   set of arguments as described below under "String DSL options".
  #
  # [<tt>Blockenspiel.invoke(<i>user_string</i>, <i>opts</i>) { ... }</tt>]
  #   This form takes the user's code as a block, while the DSL itself is
  #   specified in the given block, as described below under "Dynamic
  #   target generation". The opts hash is optional and provides a set of
  #   arguments as described below under "String DSL options".
  #
  # [<tt>Blockenspiel.invoke(<i>my_dsl</i>, <i>opts</i>)</tt>]
  #   This form reads the user's code from a file, and takes the DSL itself
  #   as an object with DSL methods. The opts hash is required and provides
  #   a set of arguments as described below under "String DSL options". The
  #   <tt>:file</tt> option is required.
  #
  # [<tt>Blockenspiel.invoke(<i>opts</i>) { ... }</tt>]
  #   This form reads the user's code from a file, while the DSL itself is
  #   specified in the given block, as described below under "Dynamic
  #   target generation". The opts hash is required and provides a set of
  #   arguments as described below under "String DSL options". The
  #   <tt>:file</tt> option is required.
  #
  # === Block DSL options
  #
  # When a user provides DSL code using a block, you simply pass that block
  # as the first parameter to Blockenspiel.invoke. Normally, Blockenspiel
  # will first check the block's arity to see whether it takes a parameter.
  # If so, it will pass the given target to the block. If the block takes
  # no parameter, and the given target is an instance of a class with DSL
  # capability, the DSL methods are made available on the caller's self
  # object so they may be called without a block parameter.
  #
  # Following are the options understood by Blockenspiel when providing
  # code using a block:
  #
  # [<tt>:parameterless</tt>]
  #   If set to false, disables parameterless blocks and always attempts to
  #   pass a parameter to the block. Otherwise, you may set it to one of
  #   three behaviors for parameterless blocks: <tt>:mixin</tt> (the
  #   default), <tt>:instance</tt>, and <tt>:proxy</tt>. See below for
  #   detailed descriptions of these behaviors. This option key is also
  #   available as <tt>:behavior</tt>.
  # [<tt>:parameter</tt>]
  #   If set to false, disables blocks with parameters, and always attempts
  #   to use parameterless blocks. Default is true, enabling parameter mode.
  #
  # The following values control the precise behavior of parameterless
  # blocks. These are values for the <tt>:parameterless</tt> option.
  #
  # [<tt>:proxy</tt>]
  #   This is the default behavior for parameterless blocks.
  #   This behavior changes +self+ to a proxy object created by applying the
  #   DSL methods to an empty object, whose <tt>method_missing</tt> points
  #   back at the block's context. This behavior is a compromise between
  #   instance and mixin. As with instance, +self+ is changed, so the caller
  #   loses access to its own instance variables. However, the caller's own
  #   methods should still be available since any methods not handled by the
  #   DSL are delegated back to the caller. Also, as with mixin, the target
  #   object's instance variables are not available (and thus cannot be
  #   clobbered) in the block, and the transformations specified by
  #   <tt>dsl_method</tt> directives are honored.
  # [<tt>:instance</tt>]
  #   This behavior changes +self+ directly to the target object using
  #   <tt>instance_eval</tt>. Thus, the caller loses access to its own
  #   helper methods and instance variables, and instead gains access to the
  #   target object's instance variables. The target object's methods are
  #   not modified: this behavior does not apply any DSL method changes
  #   specified using <tt>dsl_method</tt> directives.
  # [<tt>:mixin</tt>]
  #   This behavior is not available on all ruby platforms. DSL methods from
  #   the target are temporarily overlayed on the caller's +self+ object, but
  #   +self+ still points to the same object. Thus the helper methods and
  #   instance variables from the caller's closure remain available. The DSL
  #   methods are removed when the block completes.
  #
  # === String DSL options
  #
  # When a user provides DSL code using a string (either directly or via a
  # file), Blockenspiel always treats it as a "parameterless" invocation,
  # since there is no way to "pass a parameter" to a string. Thus, the two
  # options recognized for block DSLs, <tt>:parameterless</tt>, and
  # <tt>:parameter</tt>, are meaningless and ignored. However, the
  # following new options are recognized:
  #
  # [<tt>:file</tt>]
  #   The value of this option should be a string indicating the path to
  #   the file from which the user's DSL code is coming. It is passed
  #   as the "file" parameter to eval; that is, it is included in the stack
  #   trace should an exception be thrown out of the DSL. If no code string
  #   is provided directly, this option is required and must be set to the
  #   path of the file from which to load the code.
  # [<tt>:line</tt>]
  #   This option is passed as the "line" parameter to eval; that is, it
  #   indicates the starting line number for the code string, and is used
  #   to compute line numbers for the stack trace should an exception be
  #   thrown out of the DSL. This option is optional and defaults to 1.
  # [<tt>:behavior</tt>]
  #   Controls how the DSL is called. Recognized values are <tt>:proxy</tt>
  #   (the default) and <tt>:instance</tt>. See below for detailed
  #   descriptions of these behaviors. Note that <tt>:mixin</tt> is not
  #   allowed in this case because its behavior would be indistinguishable
  #   from the proxy behavior.
  #
  # The following values are recognized for the <tt>:behavior</tt> option:
  #
  # [<tt>:proxy</tt>]
  #   This behavior changes +self+ to a proxy object created by applying the
  #   DSL methods to an empty object. Thus, the code in the DSL string does
  #   not have access to the target object's internal instance variables or
  #   private methods. Furthermore, the transformations specified by
  #   <tt>dsl_method</tt> directives are honored. This is the default
  #   behavior.
  # [<tt>:instance</tt>]
  #   This behavior actually changes +self+ to the target object using
  #   <tt>instance_eval</tt>. Thus, the code in the DSL string gains access
  #   to the target object's instance variables and private methods. Also,
  #   the target object's methods are not modified: this behavior does not
  #   apply any DSL method changes specified using <tt>dsl_method</tt>
  #   directives.
  #
  # === Dynamic target generation
  #
  # It is also possible to dynamically generate a target object by passing
  # a block to this method. This is probably best illustrated by example:
  #
  #  Blockenspiel.invoke(block) do
  #    add_method(:set_foo) do |value|
  #      my_foo = value
  #    end
  #    add_method(:set_things_from_block) do |value, &blk|
  #      my_foo = value
  #      my_bar = blk.call
  #    end
  #  end
  #
  # The above is roughly equivalent to invoking Blockenspiel with an
  # instance of this target class:
  #
  #  class MyFooTarget
  #    include Blockenspiel::DSL
  #    def set_foo(value)
  #      set_my_foo_from(value)
  #    end
  #    def set_things_from_block(value)
  #      set_my_foo_from(value)
  #      set_my_bar_from(yield)
  #    end
  #  end
  #
  #  Blockenspiel.invoke(block, MyFooTarget.new)
  #
  # The obvious advantage of using dynamic object generation is that you are
  # creating methods using closures, which provides the opportunity to, for
  # example, modify closure local variables such as my_foo. This is more
  # difficult to do when you create a target class since its methods do not
  # have access to outside data. Hence, in the above example, we hand-waved,
  # assuming the existence of some method called "set_my_foo_from".
  #
  # The disadvantage is performance. If you dynamically generate a target
  # object, it involves parsing and creating a new class whenever it is
  # invoked. Thus, it is recommended that you use this technique for calls
  # that are not used repeatedly, such as one-time configuration.
  #
  # See the Blockenspiel::Builder class for more details on add_method.
  #
  # (And yes, you guessed it: this API is a DSL block, and is itself
  # implemented using Blockenspiel.)

  def self.invoke(*args_, &builder_block_)
    # This method itself is responsible for parsing the args to invoke,
    # and handling the dynamic target generation. It then passes control
    # to one of the _invoke_with_* methods.

    # The arguments.
    block_ = nil
    eval_str_ = nil
    target_ = nil
    opts_ = {}

    # Get the code
    case args_.first
    when ::String
      eval_str_ = args_.shift
    when ::Proc
      block_ = args_.shift
    end

    # Get the target, performing dynamic target generation if requested
    if builder_block_
      builder_ = ::Blockenspiel::Builder.new
      invoke(builder_block_, builder_)
      target_ = builder_._create_target
      args_.shift if args_.first.nil?
    else
      target_ = args_.shift
      unless target_
        raise ::ArgumentError, "No DSL target provided"
      end
    end

    # Get the options hash
    if args_.first.kind_of?(::Hash)
      opts_ = args_.shift
    end
    if args_.size > 0
      raise ::ArgumentError, "Unexpected arguments"
    end

    # Invoke
    if block_
      _invoke_with_block(block_, target_, opts_)
    else
      _invoke_with_string(eval_str_, target_, opts_)
    end
  end


  # Invoke when the DSL user provides code as a string or file.
  # We open and read the file if need be, and then pass control
  # to the _execute method.

  def self._invoke_with_string(eval_str_, target_, opts_)  # :nodoc:
    # Read options
    file_ = opts_[:file]
    line_ = opts_[:line] || 1

    # Read file if no string provided directly
    unless eval_str_
      if file_
        eval_str_ = ::File.read(file_)
      else
        raise ::ArgumentError, "No code or file provided."
      end
    else
      file_ ||= "(String passed to Blockenspiel)"
    end

    # Handle instance-eval behavior
    if opts_[:behavior] == :instance
      return target_.instance_eval(eval_str_, file_, line_)
    end

    # Execute the DSL using the proxy method.
    _execute_dsl(false, nil, eval_str_, target_, file_, line_)
  end


  # Invoke when the DSL user provides code as a block. We read the given
  # options hash, handle a few special cases, and then pass control to the
  # _execute method.

  def self._invoke_with_block(block_, target_, opts_)  # :nodoc:
    # Read options
    parameter_ = opts_[:parameter]
    parameterless_ = opts_.include?(:behavior) ? opts_[:behavior] : opts_[:parameterless]

    # Handle no-target behavior
    if parameter_ == false && parameterless_ == false
      if block_.arity != 0 && block_.arity != -1
        raise ::Blockenspiel::BlockParameterError, "Block should not take parameters"
      end
      return block_.call
    end

    # Handle parametered block case
    if parameter_ != false && block_.arity == 1 || parameterless_ == false
      if block_.arity != 1
        raise ::Blockenspiel::BlockParameterError, "Block should take exactly one parameter"
      end
      return block_.call(target_)
    end

    # Check arity for parameterless case
    if block_.arity != 0 && block_.arity != -1
      raise ::Blockenspiel::BlockParameterError, "Block should not take parameters"
    end

    # Handle instance-eval behavior
    if parameterless_ == :instance
      return target_.instance_eval(&block_)
    end

    # Execute the DSL
    _execute_dsl(parameterless_ == :mixin, block_, nil, target_, nil, nil)
  end


  # Class for proxy delegators.
  # The proxy behavior creates one of these delegators, mixes in the dsl
  # methods, and uses instance_eval to invoke the block. This class delegates
  # non-handled methods to the context object.

  class ProxyDelegator  # :nodoc:

    def initialize(delegate_)
      @_blockenspiel_delegate = delegate_
    end

    def method_missing(symbol_, *params_, &block_)
      ::Blockenspiel._proxy_dispatch(self, symbol_, params_, block_)
    end

  end


  # :stopdoc:
  NO_VALUE = ::Object.new
  # :startdoc:

  @_target_stacks = {}
  @_mixin_counts = {}
  @_proxy_delegators = {}
  @_mutex = ::Mutex.new


  # This is the "meat" of Blockenspiel, implementing both the proxy and
  # mixin methods.

  def self._execute_dsl(use_mixin_method_, block_, eval_str_, target_, file_, line_)  # :nodoc:
    # Get the module of dsl methods
    mod_ = target_.class._get_blockenspiel_module rescue nil
    unless mod_
      raise ::Blockenspiel::DSLMissingError, "Given DSL target does not include Blockenspiel::DSL"
    end

    # Get the block's calling context object
    context_object_ = block_ ? ::Kernel.eval('self', block_.binding) : nil

    if use_mixin_method_

      # Create hash keys
      mixin_count_key_ = [context_object_.object_id, mod_.object_id]
      target_stack_key_ = _current_context_id(context_object_)

      # Store the target for inheriting.
      # We maintain a target call stack per thread.
      target_stack_ = @_target_stacks[target_stack_key_] ||= []
      target_stack_.push(target_)

      # Mix this module into the object, if required.
      # This ensures that we keep track of the number of requests to
      # mix this module in, from nested blocks and possibly multiple threads.
      @_mutex.synchronize do
        count_ = @_mixin_counts[mixin_count_key_]
        if count_
          @_mixin_counts[mixin_count_key_] = count_ + 1
        else
          @_mixin_counts[mixin_count_key_] = 1
          context_object_.extend(mod_)
        end
      end

      begin

        # Now call the block
        return block_.call

      ensure

        # Clean up the target stack
        target_stack_.pop
        @_target_stacks.delete(target_stack_key_) if target_stack_.size == 0

        # Remove the mixin from the object, if required.
        @_mutex.synchronize do
          count_ = @_mixin_counts[mixin_count_key_]
          if count_ == 1
            @_mixin_counts.delete(mixin_count_key_)
            ::Blockenspiel::Unmixer.unmix(context_object_, mod_)
          else
            @_mixin_counts[mixin_count_key_] = count_ - 1
          end
        end

      end

    else

      # Create proxy object
      proxy_ = ::Blockenspiel::ProxyDelegator.new(context_object_)
      proxy_.extend(mod_)

      # Store the target object so the dispatcher can get it
      target_stack_key_ = _current_context_id(proxy_)
      @_target_stacks[target_stack_key_] = [target_]

      begin

        # Evaluate with the proxy as self
        if block_
          return proxy_.instance_eval(&block_)
        else
          return proxy_.instance_eval(eval_str_, file_, line_)
        end

      ensure

        # Clean up the dispatcher information
        @_target_stacks.delete(target_stack_key_)

      end

    end
  end


  # This implements the mapping between DSL module methods and target object methods.
  # We look up the current target object based on the current thread.
  # Then we attempt to call the given method on that object.
  # If we can't find an appropriate method to call, return the special value NO_VALUE.

  def self._target_dispatch(object_, name_, params_, block_)  # :nodoc:
    target_stack_ = @_target_stacks[_current_context_id(object_)]
    return ::Blockenspiel::NO_VALUE unless target_stack_
    target_stack_.reverse_each do |target_|
      target_class_ = target_.class
      delegate_ = target_class_._get_blockenspiel_delegate(name_)
      if delegate_ && target_class_.public_method_defined?(delegate_)
        return target_.send(delegate_, *params_, &block_)
      end
    end
    return ::Blockenspiel::NO_VALUE
  end


  # This implements the proxy fall-back behavior.
  # We look up the context object, and call the given method on that object.

  def self._proxy_dispatch(proxy_, name_, params_, block_)  # :nodoc:
    delegate_ = proxy_.instance_variable_get(:@_blockenspiel_delegate)
    if delegate_
      delegate_.send(name_, *params_, &block_)
    else
      raise ::NoMethodError, "undefined method `#{name_}' in DSL"
    end
  end


  # This returns a current context ID, which includes both the curren thread
  # object_id and the current fiber object_id (if available).

  begin
    require 'fiber'
    raise ::LoadError unless defined?(::Fiber)
    def self._current_context_id(object_)  # :nodoc:
      thid_ = ::Thread.current.object_id
      begin
        [thid_, ::Fiber.current.object_id, object_.object_id]
      rescue ::Exception
        # JRuby hack (see JRUBY-5842)
        [thid_, 0, object_.object_id]
      end
    end
  rescue ::LoadError
    def self._current_context_id(object_)  # :nodoc:
      [::Thread.current.object_id, object_.object_id]
    end
  end


end
