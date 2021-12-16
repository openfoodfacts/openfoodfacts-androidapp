# -----------------------------------------------------------------------------
#
# Blockenspiel dynamic target construction
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


module Blockenspiel


  # === Dynamically construct a target
  #
  # These methods are available in a block passed to Blockenspiel#invoke and
  # can be used to dynamically define what methods are available from a block.
  # See Blockenspiel#invoke for more information.

  class Builder

    include ::Blockenspiel::DSL


    # This is a base class for dynamically constructed targets.
    # The actual target class is an anonymous subclass of this base class.

    class Target  # :nodoc:

      include ::Blockenspiel::DSL


      # Add a method specification to the subclass.

      def self._add_methodinfo(name_, block_, yields_)
        (@_blockenspiel_methodinfo ||= {})[name_] = [block_, yields_]
        module_eval("def #{name_}(*params_, &block_); self.class._invoke_methodinfo(:#{name_}, params_, block_); end\n")
      end


      # Attempt to invoke the given method on the subclass.

      def self._invoke_methodinfo(name_, params_, block_)
        info_ = @_blockenspiel_methodinfo[name_]
        case info_[1]
        when :first
          params_.unshift(block_)
        when :last
          params_.push(block_)
        end
        info_[0].call(*params_, &block_)
      end

    end


    # Sets up the dynamic target class.

    def initialize  # :nodoc:
      @target_class = ::Class.new(::Blockenspiel::Builder::Target)
      @target_class.dsl_methods(false)
    end


    # Creates a new instance of the dynamic target class

    def _create_target  # :nodoc:
      @target_class.new
    end


    # === Declare a DSL method.
    #
    # This call creates a method that can be called from the DSL block.
    # Provide a name for the method, a block defining the method's
    # implementation, and an optional hash of options.
    #
    # By default, a method of the same name is also made available to
    # parameterless blocks. To change the name of the parameterless method,
    # provide its name as the value of the <tt>:dsl_method</tt> option.
    # To disable this method for parameterless blocks, set the
    # <tt>:dsl_method</tt> option to +false+.
    #
    # The <tt>:mixin</tt> option is a deprecated alias for
    # <tt>:dsl_method</tt>.
    #
    # === Warning about the +return+ keyword
    #
    # Because you are implementing your method using a block, remember the
    # distinction between <tt>Proc.new</tt> and +lambda+. Invoking +return+
    # from the former does not return from the block, but returns from the
    # surrounding method scope. Since normal blocks passed to methods are
    # of the former type, be very careful about using the +return+ keyword:
    #
    #  add_method(:foo) do |param|
    #    puts "foo called with parameter "+param.inspect
    #    return "a return value"   # DOESN'T WORK LIKE YOU EXPECT!
    #  end
    #
    # To return a value from the method you are creating, set the evaluation
    # value at the end of the block:
    #
    #  add_method(:foo) do |param|
    #    puts "foo called with parameter "+param.inspect
    #    "a return value"    # Returns from method foo
    #  end
    #
    # If you must use the +return+ keyword, create your block as a lambda
    # as in this example:
    #
    #  code = lambda do |param|
    #    puts "foo called with parameter "+param.inspect
    #    return "a return value"   # Returns from method foo
    #  end
    #  add_method(:foo, &code)
    #
    # === Accepting a block argument
    #
    # If you want your method to take a block, you have several options
    # depending on your Ruby version. If you are running the standard Matz
    # Ruby interpreter (MRI) version 1.8.7 or later (including 1.9.x), or a
    # compatible interpreter such as JRuby 1.5 or later, you can use the
    # standard "&" block argument notation to receive the block.
    # Note that you must call the passed block using the +call+ method since
    # Ruby doesn't support invoking such a block with +yield+.
    # For example, to create a method named "foo" that takes one parameter
    # and a block, do this:
    #
    #  add_method(:foo) do |param, &block|
    #    puts "foo called with parameter "+param.inspect
    #    puts "the block returned "+block.call.inspect
    #  end
    #
    # In your DSL, you can then call:
    #
    #  foo("hello"){ "a value" }
    #
    # If you are using MRI 1.8.6, or another Ruby interpreter that doesn't
    # fully support this syntax (such as JRuby versions older than 1.5),
    # Blockenspiel provides an alternative in the form of the <tt>:block</tt>
    # option. This option causes blocks provided by the caller to be included
    # in the normal parameter list to your method, instead of as a block
    # parameter. It can be set to <tt>:first</tt> or <tt>:last</tt> to
    # prepend or append, respectively, the block (as a +Proc+ object) to
    # the parameter list. If the caller does not include a block when
    # calling your DSL method, nil is prepended/appended. For example:
    #
    #  add_method(:foo, :block => :last) do |param, block|
    #    puts "foo called with parameter "+param.inspect
    #    if block
    #      puts "the block returned "+block.call.inspect
    #    else
    #      puts "no block passed"
    #    end
    #  end
    #
    # The <tt>:receive_block</tt> option is a deprecated alternative.
    # Setting <tt>:receive_block => true</tt> is currently equivalent to
    # setting <tt>:block => :last</tt>.

    def add_method(name_, opts_={}, &block_)
      receive_block_ = opts_[:receive_block] ? :last : opts_[:block]
      receive_block_ = :first if receive_block_ && receive_block_ != :last
      @target_class._add_methodinfo(name_, block_, receive_block_)
      dsl_method_name_ = opts_[:dsl_method] || opts_[:mixin]
      if dsl_method_name_ != false
        dsl_method_name_ = name_ if dsl_method_name_.nil? || dsl_method_name_ == true
        @target_class.dsl_method(dsl_method_name_, name_)
      end
    end

  end


end
