import { IconNavLink } from "./IconNavLink";

interface NavButtonProps {
  link: string;
  icon: React.ReactNode;
  label: string;
  onClick?: () => void;
}

const NavButton = ({ link, icon, label, onClick }: NavButtonProps) => {
  return (
    <IconNavLink
      to={link}
      icon={icon}
      placement="col"
      gap={4}
      onClick={onClick}
      aria-label={label}
    >
      {label}
    </IconNavLink>
  );
};

export default NavButton;
